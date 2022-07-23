package tech.harmonysoft.oss.common.ssl.config.impl

import tech.harmonysoft.oss.common.ssl.config.SslCertificateConfig
import tech.harmonysoft.oss.common.ssl.config.SslCertificateConfigProvider
import tech.harmonysoft.oss.common.string.util.HideValueInToString
import tech.harmonysoft.oss.common.string.util.ToStringUtil
import tech.harmonysoft.oss.common.string.util.isNotNullNotBlankEffective
import tech.harmonysoft.oss.common.string.util.isNullOrBlankEffective
import tech.harmonysoft.oss.inpertio.client.ConfigPrefix
import tech.harmonysoft.oss.inpertio.client.DelegatingConfigProvider
import tech.harmonysoft.oss.inpertio.client.factory.ConfigProviderFactory
import java.io.File
import javax.inject.Named

@Named
class SslCertificateConfigProviderImpl(
    factory: ConfigProviderFactory
) : SslCertificateConfigProvider, DelegatingConfigProvider<SslCertificateConfig>(

    factory.build(RawSslCertificateConfig::class.java) { raw ->
        if (raw.disabled == true) {
            return@build SslCertificateConfig.NoCertificate
        }
        val path = raw.path ?: return@build SslCertificateConfig.NoCertificate

        if (raw.password.isNotNullNotBlankEffective() && raw.passwordFilePath.isNotNullNotBlankEffective()) {
            throw IllegalArgumentException(
                "only password or password file can be specified for SSL certificate, got the both"
            )
        }

        if (raw.password.isNullOrBlankEffective() && raw.passwordFilePath.isNullOrBlankEffective()) {
            throw IllegalArgumentException(
                "password or password file must be specified for SSL certificate"
            )
        }

        val password = when {
            raw.password.isNotNullNotBlankEffective() -> raw.password
            raw.passwordFilePath.isNotNullNotBlankEffective() -> File(raw.passwordFilePath).readText()
            else -> throw IllegalArgumentException("I can't happen")
        }
        SslCertificateConfig.Certificate(
            path = path,
            password = password
        )
    }
)

@ConfigPrefix("ssl.certificate")
data class RawSslCertificateConfig(
    val path: String?,
    @HideValueInToString val password: String?,
    val passwordFilePath: String?,
    val disabled: Boolean?
) {

    override fun toString(): String {
        return ToStringUtil.build(this)
    }
}