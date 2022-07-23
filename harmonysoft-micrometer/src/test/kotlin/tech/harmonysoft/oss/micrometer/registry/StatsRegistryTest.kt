package tech.harmonysoft.oss.micrometer.registry

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import tech.harmonysoft.oss.common.execution.impl.ExecutionContextManagerImpl
import tech.harmonysoft.oss.common.execution.withContext
import tech.harmonysoft.oss.micrometer.auto.ContextTagsProvider
import tech.harmonysoft.oss.micrometer.util.StatsValue.NO_VALUE
import java.util.*

internal class StatsRegistryTest {

    private val testContextTags = mutableSetOf<String>()
    private val tagsProvider = object : ContextTagsProvider {
        override val contextTags: Set<String>
            get() = testContextTags
    }

    private val meter2tags = IdentityHashMap<Counter, Set<Tag>>()
    private val meterRegistry = mock<MeterRegistry> {
        on { counter(any(), any<MutableIterable<Tag>>()) } doAnswer {
            mock<Counter>().apply {
                meter2tags[this] = it.getArgument(1)
            }
        }
    }

    private val meterCreator: (MeterRegistry, Iterable<Tag>) -> Counter = { registry, tags ->
        registry.counter("measurement", tags)
    }

    private val executionContextManager = ExecutionContextManagerImpl()

    private val registryFactory: StatsRegistryFactory
        get() = StatsRegistryFactory(meterRegistry, executionContextManager, Optional.of(setOf(tagsProvider)))

    @BeforeEach
    fun setUp() {
        testContextTags.clear()
        meter2tags.clear()
    }

    @Test
    fun `when meter with two tags and the same values is used then it works correctly`() {
        val counters = registryFactory.create(tagNames = arrayOf(TAG1, TAG2), meterCreator = meterCreator)
        val counter1 = counters.getMeter("test", "test")
        val counter2 = counters.getMeter("test", "test")
        assertThat(counter1).isSameAs(counter2)
    }

    @Test
    fun `when no context info is available then a meter without tags is returned`() {
        val registry = registryFactory.create { registry, tags ->
            meterCreator(registry, tags)
        }
        val meter = registry.getMeter()
        assertThat(meter2tags[meter]).isEmpty()
        assertThat(registry.getMeter()).isSameAs(meter)
    }

    @Test
    fun `when context tag name is defined but context tag value is not provided then correct meter is used`() {
        testContextTags += TAG1
        val registry = registryFactory.create(meterCreator = meterCreator)
        val meter = registry.getMeter()
        assertThat(meter2tags[meter]).containsOnly(Tag.of(TAG1, NO_VALUE))
    }

    @Test
    fun `when context info is available then it is used in meter`() {
        testContextTags += TAG1
        val registry = registryFactory.create(meterCreator = meterCreator)

        val meter1 = registry.getMeter()
        assertThat(meter2tags[meter1]).containsOnly(Tag.of(TAG1, NO_VALUE))

        executionContextManager.withContext(TAG1, VALUE1) {
            val meter2 = registry.getMeter()
            assertThat(meter2).isNotSameAs(meter1)
            assertThat(meter2tags[meter2]).containsOnly(Tag.of(TAG1, VALUE1))
        }
        assertThat(registry.getMeter()).isSameAs(meter1)
    }

    @Test
    fun `when target tag value is given explicitly then it overwrites context value`() {
        testContextTags += TAG1
        val registry = registryFactory.create(tagNames = arrayOf(TAG1), meterCreator = meterCreator)
        executionContextManager.withContext(TAG1, VALUE1) {
            val meter = registry.getMeter(VALUE2)
            assertThat(meter2tags[meter]).containsOnly(Tag.of(TAG1, VALUE2))
        }
    }

    @Test
    fun `when there is context info and explicit overwriting value then they are combined`() {
        testContextTags += setOf(TAG1, TAG2)
        val registry = registryFactory.create(tagNames = arrayOf(TAG1), meterCreator = meterCreator)

        executionContextManager.withContext(TAG1, VALUE1) {
            executionContextManager.withContext(TAG2, VALUE2) {
                val meter = registry.getMeter(VALUE3)
                assertThat(meter2tags[meter]).containsOnly(Tag.of(TAG1, VALUE3), Tag.of(TAG2, VALUE2))
            }
            val meter = registry.getMeter(VALUE2)
            assertThat(meter2tags[meter]).containsOnly(Tag.of(TAG1, VALUE2), Tag.of(TAG2, NO_VALUE))
        }
    }

    @Test
    fun `when explicit tag is not the one used in context then they are combined`() {
        testContextTags += TAG1
        val registry = registryFactory.create(tagNames = arrayOf(TAG2), meterCreator = meterCreator)
        executionContextManager.withContext(TAG1, VALUE1) {
            val meter = registry.getMeter(VALUE2)
            assertThat(meter2tags[meter]).containsOnly(Tag.of(TAG1, VALUE1), Tag.of(TAG2, VALUE2))
        }
    }

    @Test
    fun `when no-args meter request is done against a factory with non-empty tags then an exception is thrown`() {
        val registry = registryFactory.create(tagNames = arrayOf(TAG1), meterCreator = meterCreator)
        assertThrows<IllegalArgumentException> {
            registry.getMeter()
        }
    }

    @Test
    fun `when single-args meter request is done against a factory with empty tags then an exception is thrown`() {
        val registry = registryFactory.create(tagNames = emptyArray(), meterCreator = meterCreator)
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1)
        }
    }

    @Test
    fun `when single-args meter request is done against a factory with more than one tag then an exception is thrown`() {
        val registry = registryFactory.create(tagNames = arrayOf(TAG1, TAG2), meterCreator = meterCreator)
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1)
        }
    }

    @Test
    fun `when two-args meter request is done against a factory with less than two tags then an exception is thrown`() {
        val registry = registryFactory.create(tagNames = arrayOf(TAG1), meterCreator = meterCreator)
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1, VALUE2)
        }
    }

    @Test
    fun `when two-args meter request is done against a factory with more than two tags then an exception is thrown`() {
        val registry = registryFactory.create(
            tagNames = arrayOf(TAG1, TAG2, TAG3),
            meterCreator = meterCreator
        )
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1, VALUE2)
        }
    }

    @Test
    fun `when three-args meter request is done against a factory with lesser number of tags then an exception is thrown`() {
        val registry = registryFactory.create(tagNames = arrayOf(TAG1, TAG2), meterCreator = meterCreator)
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1, VALUE2, VALUE3)
        }
    }

    @Test
    fun `when three-args meter request is done against a factory with greater number of tags then an exception is thrown`() {
        val registry = registryFactory.create(
            tagNames = arrayOf(TAG1, TAG2, TAG3, TAG4),
            meterCreator = meterCreator
        )
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1, VALUE2, VALUE3)
        }
    }

    @Test
    fun `when four-args meter request is done against a factory with lesser number of tags then an exception is thrown`() {
        val registry = registryFactory.create(
            tagNames = arrayOf(TAG1, TAG2, TAG3),
            meterCreator = meterCreator
        )
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1, VALUE2, VALUE3, VALUE4)
        }
    }

    @Test
    fun `when four-args meter request is done against a factory with greater number of tags then an exception is thrown`() {
        val registry = registryFactory.create(
            tagNames = arrayOf(TAG1, TAG2, TAG3, TAG4, TAG5),
            meterCreator = meterCreator
        )
        assertThrows<IllegalArgumentException> {
            registry.getMeter(VALUE1, VALUE2, VALUE3, VALUE4)
        }
    }

    @Test
    fun `when static tags are provided then they are attached to data points`() {
        val registry = registryFactory.create(setOf(Tag.of(TAG1, VALUE1))) { registry, tags ->
            meterCreator(registry, tags)
        }
        val meter = registry.getMeter()
        assertThat(meter2tags[meter]).containsOnly(Tag.of(TAG1, VALUE1))
        assertThat(registry.getMeter()).isSameAs(meter)
    }

    companion object {
        const val TAG1 = "tag1"
        const val TAG2 = "tag2"
        const val TAG3 = "tag3"
        const val TAG4 = "tag4"
        const val TAG5 = "tag5"

        const val VALUE1 = "value1"
        const val VALUE2 = "value2"
        const val VALUE3 = "value3"
        const val VALUE4 = "value4"
    }
}