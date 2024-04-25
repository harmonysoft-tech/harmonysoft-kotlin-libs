package tech.harmonysoft.oss.common.di

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import tech.harmonysoft.oss.common.util.CommonConstants

@ComponentScan(CommonConstants.ROOT_LIBRARY_PACKAGE)
@TestConfiguration
open class CommonTestConfig {

    @Bean
    open fun threadPool(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(1)
    }
}