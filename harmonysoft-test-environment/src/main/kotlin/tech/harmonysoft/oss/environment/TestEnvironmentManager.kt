package tech.harmonysoft.oss.environment

import java.io.File
import java.nio.file.Files
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.environment.ext.TestEnvironmentManagerMixin
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.test.util.TestUtil
import tech.harmonysoft.oss.test.util.VerificationUtil

@Named
class TestEnvironmentManager(
    private val json: JsonApi,
    private val logger: Logger,
    environments: Optional<Collection<TestEnvironment<*>>>,
    environmentInfoProvider: Optional<EnvironmentInfoProvider>,
    mixin: Optional<TestEnvironmentManagerMixin>
) {

    private val environments = environments.orElse(emptyList())
    private val environmentConfigs = ConcurrentHashMap<String, Any>()
    private val mixin = mixin.orElse(TestEnvironmentManagerMixin.NoOp)

    val testContext = TestContext(
        rootDir = Files.createTempDirectory("test").toFile().apply { deleteOnExit() },
        executionId = environmentInfoProvider.orElse(EnvironmentInfoProvider.Default).executionId
    )

    @BeforeEach
    fun startIfNecessary() {
        mixin.beforeStart(testContext)
        for (environment in environments) {
            startIfNecessary(environment)
        }
        mixin.afterStart(testContext)
    }

    fun <T : Any> startIfNecessary(environment: TestEnvironment<T>): T {
        getRunningEnvironmentConfig(environment)?.let {
            return it
        }
        logger.info("starting '{}' test environment", environment.id)
        val config = environment.start(testContext)
        logger.info("started '{}' environment, verifying if it looks good, config: {}", environment.id, config)
        VerificationUtil.verifyConditionHappens("${environment.id} is running") {
            if (environment.isRunning(config)) {
                ProcessingResult.success()
            } else {
                ProcessingResult.failure(
                    "newly started '${environment.id}' environment is not actually running, config: $config"
                )
            }
        }
        storeEnvironmentConfig(environment.id, config)
        return config
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getRunningEnvironmentConfig(environment: TestEnvironment<T>): T? {
        environmentConfigs[environment.id]?.let {
            return it as T
        }
        val config = getSharedEnvironmentConfig(environment)
        return if (config == null) {
            logger.info("no pre-stored config is found for environment {}", environment.id)
            null
        } else {
            logger.info(
                "found shared config for '{}' environment, checking if it is running ({})",
                environment.id, config
            )
            if (environment.isRunning(config)) {
                logger.info("detected that '{}' environment is available for config {}", environment.id, config)
                environmentConfigs[environment.id] = config
                config
            } else {
                logger.info("detected that '{}' environment is not available for config {}", environment.id, config)
                null
            }
        }
    }

    private fun <T : Any> getSharedEnvironmentConfig(environment: TestEnvironment<T>): T? {
        val envConfigFile = getEnvironmentConfigFile(environment.id)
        if (!envConfigFile.isFile) {
            return null
        }

        return try {
            json.parse(envConfigFile.readText(), environment.configClass)
        } catch (e: Exception) {
            logger.warn(
                "can not deserialize {} environment config from file {} data:\n{}",
                environment.id, envConfigFile.canonicalPath, envConfigFile.readText()
            )
            null
        }
    }

    private fun storeEnvironmentConfig(environmentId: String, config: Any) {
        environmentConfigs[environmentId] = config
        val configFile = getEnvironmentConfigFile(environmentId)
        if (!configFile.parentFile.isDirectory) {
            val created = configFile.parentFile.mkdirs()
            if (!created) {
                TestUtil.fail(
                    "can not create directory for storing '$environmentId' environment " +
                    "config (${configFile.parentFile.canonicalPath})"
                )
            }
        }
        configFile.writeText(json.writeJson(config))
        logger.info(
            "stored '{}' environment config into {}:\n{}",
            environmentId, configFile.canonicalPath, configFile.readText()
        )
    }


    private fun getEnvironmentConfigFile(environmentId: String): File {
        val userHomeDir = File(System.getProperty("user.home"))
        val rootConfigDir = File(userHomeDir, ".test-environment")
        val rootExecutionDir = File(rootConfigDir, testContext.executionId)
        val envConfigDir = File(rootExecutionDir, environmentId)
        return File(envConfigDir, "$environmentId-config.json")
    }
}