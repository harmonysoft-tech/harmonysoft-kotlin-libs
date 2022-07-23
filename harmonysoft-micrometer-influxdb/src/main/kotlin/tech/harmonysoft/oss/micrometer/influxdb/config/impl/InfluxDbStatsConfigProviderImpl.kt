package tech.harmonysoft.oss.micrometer.influxdb.config.impl

import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.string.util.isNullOrBlankEffective
import tech.harmonysoft.oss.inpertio.client.ConfigPrefix
import tech.harmonysoft.oss.inpertio.client.DelegatingConfigProvider
import tech.harmonysoft.oss.inpertio.client.factory.ConfigProviderFactory
import tech.harmonysoft.oss.micrometer.influxdb.config.Authentication
import tech.harmonysoft.oss.micrometer.influxdb.config.InfluxDbStatsConfig
import tech.harmonysoft.oss.micrometer.influxdb.config.InfluxDbStatsConfigProvider
import java.net.URL
import javax.inject.Named

@Named
class InfluxDbStatsConfigProviderImpl(
    factory: ConfigProviderFactory
) : InfluxDbStatsConfigProvider, DelegatingConfigProvider<InfluxDbStatsConfig>(

    factory.build(RawInfluxDbStatsConfig::class.java) { raw ->
        when {
            raw.enabled == false -> {
                LOGGER.info("InfluxDB stats publishing is disabled")
                InfluxDbStatsConfig.Disabled
            }

            raw.db.isNullOrBlankEffective() -> {
                LOGGER.info("No InfluxDB database is configured - InfluxDB stats publishing is disabled")
                InfluxDbStatsConfig.Disabled
            }

            raw.url.isNullOrBlankEffective() -> {
                LOGGER.info("No InfluxDB url is configured - InfluxDB stats publishing is disabled")
                InfluxDbStatsConfig.Disabled
            }

            else -> {
                try {
                    URL(raw.url.trim())
                    InfluxDbStatsConfig.Enabled(
                        db = raw.db.trim(),
                        url = raw.url.trim(),
                        authentication = parseAuthentication(raw)
                    )
                } catch (e: Exception) {
                    LOGGER.warn(
                        "Malformed InfluxDB stats publishing url ({}} - InfluxDB stats publishing is disabled",
                        raw.url, e)
                    InfluxDbStatsConfig.Disabled
                }
            }
        }
    }
) {
    companion object {

        private val LOGGER = LoggerFactory.getLogger(InfluxDbStatsConfigProviderImpl::class.java)

        private fun parseAuthentication(raw: RawInfluxDbStatsConfig): Authentication {
            return when {
                raw.login.isNullOrBlankEffective() -> {
                    LOGGER.info("No user is configured for InfluxDB stats publishing, don't use any authentication")
                    Authentication.No
                }

                raw.password.isNullOrBlankEffective() -> {
                    LOGGER.info("No password is configured for InfluxDB stats publishing, don't use any authentication")
                    Authentication.No
                }

                else -> Authentication.Basic(
                    login = raw.login.trim(),
                    password = raw.password.trim()
                )
            }
        }
    }
}

@ConfigPrefix("stats.influxdb")
data class RawInfluxDbStatsConfig(
    val enabled: Boolean?,
    val db: String?,
    val url: String?,
    val login: String?,
    val password: String?
)