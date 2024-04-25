package tech.harmonysoft.oss.environment.bin

import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.util.CommonConstants
import tech.harmonysoft.oss.environment.TestEnvironmentManager
import tech.harmonysoft.oss.test.util.VerificationUtil

@ComponentScan(CommonConstants.ROOT_LIBRARY_PACKAGE)
@SpringBootApplication
class TestEnvironmentChecker {
    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("start checking if test environment is ready")
            CommonBinConfigurer.configure()
            val context = runApplication<TestEnvironmentStarter>(*args)
            val envManager = context.getBean(TestEnvironmentManager::class.java)
            VerificationUtil.verifyConditionHappens(
                description = "test environment is started",
                checkTtlSeconds = TimeUnit.MINUTES.toSeconds(5),
            ) {
                envManager.checkIfEnvironmentIsReady()?.let {
                    ProcessingResult.failure(it)
                } ?: ProcessingResult.success()
            }
            logger.info("verified that test environment is available")
        }
    }
}