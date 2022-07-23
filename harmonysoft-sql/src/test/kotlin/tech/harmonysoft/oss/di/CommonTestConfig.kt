package tech.harmonysoft.oss.di

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@ComponentScan("tech.harmonysoft.oss")
@TestConfiguration
open class CommonTestConfig {

    @Bean
    open fun threadPool(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(1)
    }
}