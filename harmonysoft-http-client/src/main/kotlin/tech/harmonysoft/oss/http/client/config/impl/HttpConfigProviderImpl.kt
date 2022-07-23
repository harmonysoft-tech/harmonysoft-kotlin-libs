package tech.harmonysoft.oss.http.client.config.impl

import tech.harmonysoft.oss.common.ssl.config.SslCertificateConfig
import tech.harmonysoft.oss.common.ssl.config.SslCertificateConfigProvider
import tech.harmonysoft.oss.http.client.config.HttpConfig
import tech.harmonysoft.oss.http.client.config.HttpConfigProvider
import tech.harmonysoft.oss.http.client.config.ProxyConfig
import tech.harmonysoft.oss.inpertio.client.ConfigPrefix
import tech.harmonysoft.oss.inpertio.client.DelegatingConfigProvider
import tech.harmonysoft.oss.inpertio.client.factory.ConfigProviderFactory
import javax.inject.Named

@Named
class HttpConfigProviderImpl(
    factory: ConfigProviderFactory,
    sslCertificateConfigProvider: SslCertificateConfigProvider
) : HttpConfigProvider, DelegatingConfigProvider<HttpConfig>(

    factory.build(listOf(
        sslCertificateConfigProvider,
        factory.raw(RawHttpsApplicabilityConfig::class.java),
        factory.raw(RawHttpProxyConfigWrapper::class.java)
    )) { source ->
        val httpsApplicabilityConfig = source.get(RawHttpsApplicabilityConfig::class.java)
        val sslCertificateConfig = source.get(SslCertificateConfig::class.java)
        if (httpsApplicabilityConfig.enabled == true
            && sslCertificateConfig is SslCertificateConfig.NoCertificate
        ) {
            throw IllegalArgumentException(
                "HTTP client is configured to use HTTPS but no SSL certificate is configured"
            )
        }

        val sslCertificateConfigToUse = if (httpsApplicabilityConfig.enabled == false) {
            SslCertificateConfig.NoCertificate
        } else {
            sslCertificateConfig
        }

        val proxy = source.get(RawHttpProxyConfigWrapper::class.java).proxy?.let { rawProxy ->
            ProxyConfig(
                host = rawProxy.host,
                port = rawProxy.port,
                destinationsToProxy = rawProxy.destinationsToProxy.values.mapNotNull { rawDestination ->
                    rawDestination.destination.takeIf { rawDestination.enabled != false }
                }.toSet()
            )
        }

        HttpConfig(
            ssl = sslCertificateConfigToUse,
            proxy = proxy
        )
    }
)

@ConfigPrefix("http.client.https")
data class RawHttpsApplicabilityConfig(
    val enabled: Boolean?
)

@ConfigPrefix("http.client")
data class RawHttpProxyConfigWrapper(
    val proxy: RawHttpProxyConfig?
)

data class RawHttpProxyConfig(
    val host: String,
    val port: Int,
    val destinationsToProxy: Map<String, RawDestinationToProxy>
)

data class RawDestinationToProxy(
    val destination: String,
    val enabled: Boolean?
)