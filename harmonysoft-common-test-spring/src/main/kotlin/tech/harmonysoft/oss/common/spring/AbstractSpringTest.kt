package tech.harmonysoft.oss.common.spring

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import tech.harmonysoft.oss.test.TestAware
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [TestPropertiesInitializer::class])
abstract class AbstractSpringTest {

    private val logger = LoggerFactory.getLogger(AbstractSpringTest::class.java)

    @Autowired private lateinit var testCallbacks: Optional<Collection<TestAware>>

    @BeforeEach
    fun onTestStart(testInfo: TestInfo) {
        logger.info("Starting test '{}'", testInfo.displayName)
        testCallbacks.ifPresent {
            for (callback in it) {
                callback.onTestStart()
            }
        }
    }

    @AfterEach
    fun onTestEnd(testInfo: TestInfo) {
        testCallbacks.ifPresent {
            for (callback in it) {
                callback.onTestEnd()
            }
        }
        logger.info("Finished test '{}'", testInfo.displayName)
    }
}