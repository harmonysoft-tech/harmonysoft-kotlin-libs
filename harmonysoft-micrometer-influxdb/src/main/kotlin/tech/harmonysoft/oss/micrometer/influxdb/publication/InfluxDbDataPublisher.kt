package tech.harmonysoft.oss.micrometer.influxdb.publication

import io.micrometer.core.instrument.MeterRegistry
import org.apache.hc.core5.http.HttpHeaders
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.http.client.HttpClient
import tech.harmonysoft.oss.http.client.response.HttpResponseConverter
import tech.harmonysoft.oss.micrometer.influxdb.config.Authentication
import tech.harmonysoft.oss.micrometer.influxdb.config.InfluxDbStatsConfig
import tech.harmonysoft.oss.micrometer.influxdb.config.InfluxDbStatsConfigProvider
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Named
import javax.inject.Provider

/**
 * The main idea here is to avoid new buffers construction on publishing stats data into InfluxDB.
 *
 * This is done because profiling shows that significant amount of memory is consumed during InfluxDB
 * publishing with regular micrometer publisher.
 */
@Named
class InfluxDbDataPublisher(
    private val httpClient: Provider<HttpClient>,
    private val influxDbConfigProvider: InfluxDbStatsConfigProvider,

    meterRegistry: MeterRegistry
) {

    private val logger = LoggerFactory.getLogger(InfluxDbDataPublisher::class.java)

    private val dataExtractor: StringBuilderExtractor = StringBuilderExtractor.extractor

    private val cachedUrl = AtomicReference<Pair<InfluxDbStatsConfig.Enabled, String>?>()

    private val cachedAuthenticationHeader = AtomicReference<Pair<Authentication.Basic, Map<String, String>>>()

    private val lock = ReentrantLock()

    private val bOut = NakedByteArrayOutputStream(1 shl 16)

    private val bIn = NakedByteArrayInputStream()

    private val httpEntity = InfluxDataHttpEntity(bIn)

    init {
        meterRegistry.gauge("stats.influxdb.publish.buffer.bytes", bOut) {
            it.size().toDouble()
        }
    }

    fun publish(data: StringBuilder) {
        if (lock.tryLock()) {
            try {
                doPublish(data)
            } catch (e: Exception) {
                logger.info("Unable to publish stats data to InfluxDB", e)
            } finally {
                lock.unlock()
            }
        } else {
            logger.warn("Can't publish InfluxDB stats - there is an ongoing request. Skipping new request",
                        IllegalStateException())
        }
    }

    private fun doPublish(data: StringBuilder) {
        val config = influxDbConfigProvider.data
        if (config !is InfluxDbStatsConfig.Enabled) {
            return
        }

        val url = getUrl(config)
        val headers = getHeaders(config)

        bOut.reset()
        dataExtractor.writeDataToStream(data, httpEntity.encoding, bOut)
        bIn.updateState(bOut.rawData, bOut.size())

        val response = httpClient.get().post(
            url = url,
            headers = headers,
            entity = httpEntity,
            converter = HttpResponseConverter.BYTE_ARRAY
        )

        if (response.status >= 300) {
            logger.warn("Failed to publish stats data to InfluxDB, got response {}", response.status)
        }
    }

    private fun getUrl(config: InfluxDbStatsConfig.Enabled): String {
        val cachedUrlRecord = cachedUrl.get()
        return if (cachedUrlRecord != null && cachedUrlRecord.first == config) {
            cachedUrlRecord.second
        } else {
            "${config.url}/write?consistency=one&precision=ms&db=${config.db}".apply {
                cachedUrl.set(config to this)
            }
        }
    }

    private fun getHeaders(config: InfluxDbStatsConfig.Enabled): Map<String, String> {
        val authentication = config.authentication
        return if (authentication is Authentication.Basic) {
            val cachedAuthenticationRecord = cachedAuthenticationHeader.get()
            if (cachedAuthenticationRecord != null && cachedAuthenticationRecord.first == authentication) {
                cachedAuthenticationRecord.second
            } else {
                val toEncode = "${authentication.login}:${authentication.password}"
                val headerValue = "Basic ${Base64.getEncoder().withoutPadding().encodeToString(toEncode.toByteArray())}"
                mapOf(HttpHeaders.AUTHORIZATION to headerValue).apply {
                    cachedAuthenticationHeader.set(authentication to this)
                }
            }
        } else {
            emptyMap()
        }
    }
}