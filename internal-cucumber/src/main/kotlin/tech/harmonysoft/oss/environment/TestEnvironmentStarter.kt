package tech.harmonysoft.oss.environment

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import tech.harmonysoft.oss.common.util.CommonConstants.ROOT_LIBRARY_PACKAGE

@SpringBootApplication
@ComponentScan(ROOT_LIBRARY_PACKAGE)
open class TestEnvironmentStarter {
    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val context = runApplication<TestEnvironmentStarter>(*args)
            val envManager = context.getBean(TestEnvironmentManager::class.java)
            envManager.startIfNecessary()
            logger.info("test environment is ready, start waiting")
            while (true) {
                Thread.currentThread().join()
            }
        }
    }
}