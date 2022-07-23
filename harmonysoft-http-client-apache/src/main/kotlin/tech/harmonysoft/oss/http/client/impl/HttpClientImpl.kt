package tech.harmonysoft.oss.http.client.impl

import org.apache.hc.client5.http.SystemDefaultDnsResolver
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.socket.ConnectionSocketFactory
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.http.URIScheme
import org.apache.hc.core5.http.config.RegistryBuilder
import org.apache.hc.core5.pool.PoolConcurrencyPolicy
import org.apache.hc.core5.pool.PoolReusePolicy
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.util.TimeValue
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.ssl.config.SslCertificateConfig
import tech.harmonysoft.oss.http.client.HttpClient
import tech.harmonysoft.oss.http.client.HttpListener
import tech.harmonysoft.oss.http.client.config.HttpConfigProvider
import tech.harmonysoft.oss.http.client.response.HttpResponse
import tech.harmonysoft.oss.http.client.response.HttpResponseConverter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.zip.GZIPInputStream
import javax.inject.Named
import javax.net.ssl.SSLContext
import kotlin.concurrent.read
import kotlin.concurrent.write

@Named
class HttpClientImpl(
    private val httpConfigProvider: HttpConfigProvider,
    private val listeners: Optional<Collection<HttpListener>>,
    private val client: CloseableHttpClient
) : HttpClient {

    private val logger = LoggerFactory.getLogger(HttpClientImpl::class.java)
    private val lock = ReentrantReadWriteLock(true)

    constructor(
        httpConfigProvider: HttpConfigProvider,
        listeners: Optional<Collection<HttpListener>>
    ) : this(
        httpConfigProvider = httpConfigProvider,
        listeners = listeners,
        client = buildClient(httpConfigProvider)
    )

    override fun <T> execute(
        request: HttpUriRequestBase,
        converter: HttpResponseConverter<T>,
        headers: Map<String, String>
    ): HttpResponse<T> {
        maybeConfigureProxy(request)

        for ((header, value) in headers) {
            request.addHeader(header, value)
        }

        listeners.ifPresent {
            for (listener in it) {
                try {
                    listener.onRequest(request)
                } catch (e: Exception) {
                    logger.warn("Unexpected exception occurred on attempt to notify listeners on HTTP call start ({})",
                                request.uri, e)
                }
            }
        }

        val response = lock.read {
            client.execute(request)
        }

        listeners.ifPresent {
            for (listener in it) {
                try {
                    listener.onResponse(request, response)
                } catch (e: Exception) {
                    logger.warn("Unexpected exception occurred on attempt to notify listeners on HTTP call end ({})",
                                request.uri, e)
                }
            }
        }

        return try {
            HttpResponse(
                status = response.code,
                statusText = response.reasonPhrase ?: "",
                headers = response.headers.associate { it.name to it.value },
                body = extractResponseBody(response, converter)
            )
        } finally {
            try {
                response.close()
            } catch (e: Exception) {
                logger.warn("Unexpected exception occurred on attempt to close HTTP request to {}",
                            request.uri, e)
            }
        }
    }

    private fun maybeConfigureProxy(request: HttpUriRequestBase) {
        httpConfigProvider.data.proxy?.let { proxyConfig ->
            val useProxy = proxyConfig.destinationsToProxy?.any {
                request.authority.hostName.contains(it)
            } ?: true
            if (useProxy) {
                request.config = RequestConfig.custom().setProxy(HttpHost(proxyConfig.host, proxyConfig.port)).build()
            }
        }
    }

    private fun <T> extractResponseBody(response: CloseableHttpResponse, converter: HttpResponseConverter<T>): T {
        val entity = response.entity ?: return converter.convert(EMPTY_ARRAY, DEFAULT_CHARSET)
        var inputStream = entity.content
        val encoding = response.entity.contentEncoding
        if (encoding != null && encoding.lowercase().trim() == "gzip") {
            inputStream = GZIPInputStream(inputStream)
        }
        val rawInput = if (inputStream is ByteArrayInputStream) {
            val size = inputStream.available()
            ByteArray(size).apply {
                inputStream.read(this)
            }
        } else {
            val bOut = ByteArrayOutputStream()
            inputStream.copyTo(bOut)
            bOut.toByteArray()
        }
        val charset = encoding?.let {
            CHARSET_PATTERN.find(it)?.let { match ->
                Charset.forName(match.groupValues[1])
            }
        } ?: DEFAULT_CHARSET
        return converter.convert(rawInput, charset)
    }

    override fun close() {
        lock.write {
            client.close()
        }
    }

    companion object {
        private val CHARSET_PATTERN = """\bcharset\s*=\s*"?([^\s;"]+)""".toRegex(RegexOption.IGNORE_CASE)
        private val EMPTY_ARRAY = ByteArray(0)
        private val DEFAULT_CHARSET = Charsets.UTF_8

        private fun buildClient(httpConfigProvider: HttpConfigProvider): CloseableHttpClient {
            val builder = HttpClients.custom()
            val httpConfig = httpConfigProvider.data
            configureSsl(httpConfig.ssl, builder)
            return builder.build()
        }

        private fun configureSsl(config: SslCertificateConfig, builder: HttpClientBuilder) {
            when (config) {
                is SslCertificateConfig.NoCertificate -> {
                    val ssl = SSLContexts.custom()
                        .loadTrustMaterial(null) { _, _ -> true }
                        .build()
                    configureSsl(ssl, builder)
                }

                is SslCertificateConfig.Certificate -> {
                    val password = config.password?.toCharArray()
                    val keyStore = KeyStore.getInstance("PKCS12")
                    val resource = this::class.java.getResourceAsStream(config.path)
                                   ?: throw IllegalArgumentException(
                                       "can't find an HTTP certificate at path '${config.path}'"
                                   )
                    keyStore.load(resource, password)
                    val ssl = SSLContexts.custom()
                        .loadKeyMaterial(keyStore, password)
                        .loadTrustMaterial(keyStore) { _, _ -> true }
                        .build()
                    configureSsl(ssl, builder)
                }
            }
        }

        private fun configureSsl(context: SSLContext, builder: HttpClientBuilder) {
            val registry = RegistryBuilder.create<ConnectionSocketFactory>()
                .register(URIScheme.HTTP.name, PlainConnectionSocketFactory.INSTANCE)
                .register(URIScheme.HTTPS.name, SSLConnectionSocketFactory(context))
                .build()

            val connectionManager = PoolingHttpClientConnectionManager(
                registry,
                PoolConcurrencyPolicy.STRICT,
                PoolReusePolicy.LIFO,
                TimeValue.ofMinutes(5),
                null,
                SystemDefaultDnsResolver(),
                null
            )

            builder.setConnectionManager(connectionManager)
        }
    }
}