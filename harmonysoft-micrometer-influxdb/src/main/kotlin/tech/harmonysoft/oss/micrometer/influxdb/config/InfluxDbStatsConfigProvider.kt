package tech.harmonysoft.oss.micrometer.influxdb.config

import io.micrometer.core.instrument.step.StepRegistryConfig
import tech.harmonysoft.oss.common.string.util.HideValueInToString
import tech.harmonysoft.oss.common.string.util.ToStringUtil
import tech.harmonysoft.oss.inpertio.client.ConfigProvider

interface InfluxDbStatsConfigProvider : ConfigProvider<InfluxDbStatsConfig>, StepRegistryConfig {

    override fun prefix(): String {
        return with(data) {
            when (this) {
                is InfluxDbStatsConfig.Enabled -> prefix()
                is InfluxDbStatsConfig.Disabled -> ""
            }
        }
    }

    override fun get(key: String): String? {
        return with(data) {
            when (this) {
                is InfluxDbStatsConfig.Enabled -> get(key)
                is InfluxDbStatsConfig.Disabled -> ""
            }
        }
    }
}

sealed class InfluxDbStatsConfig {

    object Disabled : InfluxDbStatsConfig()

    data class Enabled(
        val db: String,
        val url: String,
        val authentication: Authentication
    ) : InfluxDbStatsConfig(), StepRegistryConfig {

        override fun prefix() = "harmonysoft.stats.influxdb"

        override fun get(key: String): String? {
            return null
        }
    }
}

sealed class Authentication {

    object No : Authentication()

    data class Basic(
        val login: String,
        @HideValueInToString val password: String
    ) : Authentication() {

        override fun toString(): String {
            return ToStringUtil.build(this)
        }
    }
}