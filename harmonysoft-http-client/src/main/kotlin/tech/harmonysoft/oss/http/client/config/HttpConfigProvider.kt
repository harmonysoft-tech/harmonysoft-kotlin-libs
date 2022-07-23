package tech.harmonysoft.oss.http.client.config

import tech.harmonysoft.oss.common.ssl.config.SslCertificateConfig
import tech.harmonysoft.oss.inpertio.client.ConfigProvider

interface HttpConfigProvider : ConfigProvider<HttpConfig>

data class HttpConfig(
    val ssl: SslCertificateConfig,
    val proxy: ProxyConfig?
)

data class ProxyConfig(
    val host: String,
    val port: Int,

    /**
     * We might want to use proxy only for some hosts, this property allows to configure that
     */
    val destinationsToProxy: Set<String>?
)