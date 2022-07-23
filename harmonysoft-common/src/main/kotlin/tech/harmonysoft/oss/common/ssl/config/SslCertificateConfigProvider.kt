package tech.harmonysoft.oss.common.ssl.config

import tech.harmonysoft.oss.common.string.util.HideValueInToString
import tech.harmonysoft.oss.common.string.util.ToStringUtil
import tech.harmonysoft.oss.inpertio.client.ConfigProvider

interface SslCertificateConfigProvider : ConfigProvider<SslCertificateConfig>

sealed class SslCertificateConfig {

    object NoCertificate : SslCertificateConfig()

    data class Certificate(
        val path: String,
        @HideValueInToString val password: String?
    ) : SslCertificateConfig() {

        override fun toString(): String {
            return ToStringUtil.build(this)
        }
    }
}