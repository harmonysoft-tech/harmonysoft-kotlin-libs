package tech.harmonysoft.oss.common.di

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Configuration
open class HarmonysoftCommonConfiguration {

    @Bean
    open fun threadPool(@Value("\${harmonysoft.thread.pool.size:10}") threadsNumber: Int): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(threadsNumber)
    }
}