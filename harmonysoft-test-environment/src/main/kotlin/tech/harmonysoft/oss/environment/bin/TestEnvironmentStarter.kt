package tech.harmonysoft.oss.environment.bin

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import tech.harmonysoft.oss.common.util.CommonConstants
import tech.harmonysoft.oss.environment.TestEnvironmentManager

@ComponentScan(CommonConstants.ROOT_LIBRARY_PACKAGE)
@SpringBootApplication
class TestEnvironmentStarter {
    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("starting test environment")
            CommonBinConfigurer.configure()
            val context = runApplication<TestEnvironmentStarter>(*args)
            val envManager = context.getBean(TestEnvironmentManager::class.java)
            val pidFile = envManager.environmentProcessPidFile
            if (pidFile.isFile) {
                pidFile.delete()
            }

            envManager.startIfNecessary()

            if (!pidFile.parentFile.exists()) {
                pidFile.parentFile.mkdirs()
            }
            pidFile.createNewFile()
            val pid = ProcessHandle.current().pid().toString()
            pidFile.writeText(pid)

            val wait = args.isNotEmpty() && args[0] == "wait=true"
            if (wait) {
                logger.info("started test environment, waiting indefinitely")
                while (true) {
                    Thread.currentThread().join()
                }
            } else {
                logger.info("started test environment")
            }
        }
    }
}