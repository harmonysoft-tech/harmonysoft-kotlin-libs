package tech.harmonysoft.oss.environment.bin

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import tech.harmonysoft.oss.common.cli.CommandLineHelper
import tech.harmonysoft.oss.common.util.CommonConstants
import tech.harmonysoft.oss.environment.TestEnvironmentManager
import tech.harmonysoft.oss.environment.ext.TestEnvironmentManagerMixin

@ComponentScan(CommonConstants.ROOT_LIBRARY_PACKAGE)
@SpringBootApplication
class TestEnvironmentStopper {
    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            CommonBinConfigurer.configure()
            val context = runApplication<TestEnvironmentStopper>(*args)
            val envManager = context.getBean(TestEnvironmentManager::class.java)
            val mixin = context.getBeansOfType(TestEnvironmentManagerMixin::class.java).values.firstOrNull()
            val cli = context.getBean(CommandLineHelper::class.java)
            val pidFile = envManager.environmentProcessPidFile
            if (pidFile.isFile) {
                val pid = pidFile.readText().toLong()
                logger.info("trying to kill test environment process with PID $pid")
                cli.execute(
                    commandLine = "kill -9 $pid",
                    commandDescription = "kill test environment process",
                )
            }
            mixin?.beforeStop(envManager.testContext)
        }
    }
}