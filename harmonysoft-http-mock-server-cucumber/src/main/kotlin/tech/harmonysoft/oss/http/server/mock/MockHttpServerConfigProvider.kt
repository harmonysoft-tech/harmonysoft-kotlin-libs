package tech.harmonysoft.oss.cucumber.glue

import tech.harmonysoft.oss.inpertio.client.ConfigProvider

interface MockHttpServerConfigProvider : ConfigProvider<MockHttpServerConfig>

data class MockHttpServerConfig(
    val port: Int
)