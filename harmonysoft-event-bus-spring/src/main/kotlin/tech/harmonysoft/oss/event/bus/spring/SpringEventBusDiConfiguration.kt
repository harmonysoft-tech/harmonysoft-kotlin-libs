package tech.harmonysoft.oss.event.bus.spring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.harmonysoft.oss.event.bus.EventBus
import tech.harmonysoft.oss.event.bus.EventBusFactory

@Configuration
open class SpringEventBusDiConfiguration {

    @Bean
    open fun eventBus(factory: EventBusFactory): EventBus {
        return factory.async()
    }
}