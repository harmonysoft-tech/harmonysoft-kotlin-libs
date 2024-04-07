package tech.harmonysoft.oss.http.server.mock.config

import tech.harmonysoft.oss.inpertio.client.ConfigProvider
import tech.harmonysoft.oss.inpertio.client.DelegatingConfigProvider
import jakarta.inject.Named

@Named
class MockHttpServerConfigProviderImpl(
    portHolder: PortHolder
) : MockHttpServerConfigProvider, DelegatingConfigProvider<MockHttpServerConfig>(

    ConfigProvider.fixed(MockHttpServerConfig(portHolder.port))
)