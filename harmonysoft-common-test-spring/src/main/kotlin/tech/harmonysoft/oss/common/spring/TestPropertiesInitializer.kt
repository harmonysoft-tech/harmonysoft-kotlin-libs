package tech.harmonysoft.oss.common.spring

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils

/**
 * Allows to bundle particular properties within test jar and apply them automatically.
 *
 * For example, target production http client jar might require specifying client certificate by default.
 * However, we might not need that in tests and allow to disable that restriction via particular property.
 * Then we can bundle a file name like `harmonysoft-http-test.properties` into `http-client.jar` which
 * suppresses certificate check.
 */
class TestPropertiesInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(context: ConfigurableApplicationContext) {
        val resources = context.getResources("classpath*:harmonysoft-*-test.properties")
        for (resource in resources) {
            TestPropertySourceUtils.addPropertiesFilesToEnvironment(context, "classpath:${resource.filename}")
        }
    }
}