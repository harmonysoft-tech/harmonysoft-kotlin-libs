package tech.harmonysoft.oss.common.execution.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import tech.harmonysoft.oss.common.execution.withContext
import tech.harmonysoft.oss.common.execution.withCurrentContext
import java.util.concurrent.atomic.AtomicBoolean

class ExecutionContextManagerImplTest {

    private lateinit var manager: ExecutionContextManagerImpl

    @BeforeEach
    fun setUp() {
        manager = ExecutionContextManagerImpl()
    }

    @Test
    fun `when no value is in context for target key then 'null' is returned`() {
        manager.withContext("key1", "value1") {
            assertThat(manager.getFromCurrentContext<String>("key2")).isNull()
        }
    }

    @Test
    fun `when a value is in context for target key then it's returned`() {
        val key = "key"
        val value = "value"
        manager.withContext(key, value) {
            assertThat(manager.getFromCurrentContext<String>(key)).isEqualTo(value)
        }
    }

    @Test
    fun `when a value is overwritten in nested call then correct value is returned`() {
        val key = "key"
        manager.withContext(key, "value1") {
            manager.withContext(key, "value2") {
                assertThat(manager.getFromCurrentContext<String>(key)).isEqualTo("value2")
            }
            assertThat(manager.getFromCurrentContext<String>(key)).isEqualTo("value1")
        }
    }

    @Test
    fun `when current call is ended then its context value is cleared`() {
        val key = "key"
        manager.withContext(key, "value") {}
        assertThat(manager.getFromCurrentContext<String>(key)).isNull()
    }

    @Test
    fun `when null context value is applied then it's respected`() {
        val key = "key"
        manager.withContext(key, "some-value") {
            manager.withContext(key, null) {
                assertThat(manager.getFromCurrentContext<String>(key)).isNull()
            }
        }
    }

    @Test
    fun `when a context is bound to an action then it's preserved`() {
        val key1 = "key1"
        val value11 = "value11"
        val value12 = "value12"

        val key2 = "key2"
        val value2 = "value2"

        val key3 = "key3"
        val value3 = "value3"

        val tested = AtomicBoolean()

        val action = manager.withContext(key1, value11) {
            manager.withContext(key2, value2) {
                manager.withCurrentContext { ->
                    tested.set(true)
                    assertThat(manager.getFromCurrentContext<String>(key1)).isEqualTo(value11)
                    assertThat(manager.getFromCurrentContext<String>(key2)).isEqualTo(value2)
                    assertThat(manager.getFromCurrentContext<String>(key3)).isEqualTo(value3)
                }
            }
        }

        manager.withContext(key1, value12) { // changed value
            // no value for 'key2'
            manager.withContext(key3, value3) {
                action()
            }
        }

        assertThat(tested.get()).isTrue()
    }

    @Test
    fun `when custom context key duration ends then its MDC value is restored`() {
        val key = "key"
        val value1 = "value1"
        val value2 = "value2"
        MDC.put(key, value1)
        manager.withContext(key, value2) {
            assertThat(MDC.get(key)).isEqualTo(value2)
        }
        assertThat(MDC.get(key)).isEqualTo(value1)
    }

    @Test
    fun `when custom composite context duration ends then all MDC values are restored`() {
        val key1 = "key1"
        val value11 = "value11"
        val value12 = "value12"

        val key2 = "key2"
        val value21 = "value21"
        val value22 = "value22"

        MDC.put(key1, value11)
        MDC.put(key2, value21)

        val action = manager.withContext(key1, value12) {
            manager.withContext(key2, value22) {
                manager.withCurrentContext { ->
                    assertThat(MDC.get(key1)).isEqualTo(value12)
                    assertThat(MDC.get(key2)).isEqualTo(value22)
                }
            }
        }
        action()

        assertThat(MDC.get(key1)).isEqualTo(value11)
        assertThat(MDC.get(key2)).isEqualTo(value21)
    }

    @Test
    fun `when context is used in coroutine then it's preserved`() {
        val key = "key"
        runBlocking {
            manager.withContext(key, "value") {
                launch(Dispatchers.IO + manager.currentCoroutineContextElements) {
                    manager.withContext(key, "value2") {
                        delay(100)
                        assertThat(manager.getFromCurrentContext<String>(key)).isEqualTo("value2")
                    }
                    assertThat(manager.getFromCurrentContext<String>(key)).isEqualTo("value")
                }
                assertThat(manager.getFromCurrentContext<String>(key)).isEqualTo("value")
            }
        }
    }
}