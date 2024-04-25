package tech.harmonysoft.oss.environment.starter

import org.slf4j.LoggerFactory
import org.springframework.boot.runApplication
import tech.harmonysoft.oss.environment.TestEnvironmentManager

class TestEnvironmentStarter {
    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("starting test environment")
            CommonBinConfigurer.configure()
            val context = runApplication<TestEnvironmentStarter>(*args)
            val envManager = context.getBean(TestEnvironmentManager::class.java)
            envManager.startIfNecessary()
            logger.info("started test environment")
        }
    }
}