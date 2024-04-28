package tech.harmonysoft.oss.http.server.mock.config

import tech.harmonysoft.oss.configurario.client.ConfigProvider

interface MockHttpServerConfigProvider : ConfigProvider<MockHttpServerConfig>

data class MockHttpServerConfig(
    val port: Int
)