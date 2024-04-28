package tech.harmonysoft.oss.http.server.mock.config

import tech.harmonysoft.oss.configurario.client.ConfigProvider
import tech.harmonysoft.oss.configurario.client.DelegatingConfigProvider
import jakarta.inject.Named

@Named
class MockHttpServerConfigProviderImpl(
    portHolder: PortHolder
) : MockHttpServerConfigProvider, DelegatingConfigProvider<MockHttpServerConfig>(

    ConfigProvider.fixed(MockHttpServerConfig(portHolder.port))
)