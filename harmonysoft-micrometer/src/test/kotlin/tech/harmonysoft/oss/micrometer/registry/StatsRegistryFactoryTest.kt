package tech.harmonysoft.oss.micrometer.registry

import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import tech.harmonysoft.oss.common.execution.impl.ExecutionContextManagerImpl
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class StatsRegistryFactoryTest {

    private val executionContextManager = ExecutionContextManagerImpl()

    @Test
    fun `when utility timer creation method is used then explicit tag values are respected`() {
        val measurementName = "test"
        val expectedTags = listOf(ImmutableTag("tag1", "value1"), ImmutableTag("tag2", "value2"))
        val lastMeasurementName = AtomicReference<String>()
        val lastTags = AtomicReference<MutableIterable<Tag>>()
        val meterRegistry = mock<MeterRegistry> {
            on { timer(any(), any<MutableIterable<Tag>>()) } doAnswer {
                lastMeasurementName.set(it.getArgument(0))
                lastTags.set(it.getArgument(1))
                mock()
            }
        }

        val factory = StatsRegistryFactory(meterRegistry, executionContextManager, Optional.empty())
        factory.timers(measurementName, "tag1", "tag2").getMeter("value1", "value2")

        assertThat(lastMeasurementName.get()).isEqualTo(measurementName)
        assertThat(lastTags.get()).containsAnyElementsOf(expectedTags)
    }

    @Test
    fun `when static tags are configured for timers then they are attached to data points`() {
        val measurementName = "test"
        val expectedTags = listOf(ImmutableTag("tag1", "value1"), ImmutableTag("tag2", "value2"))
        val lastMeasurementName = AtomicReference<String>()
        val lastTags = AtomicReference<MutableIterable<Tag>>()
        val meterRegistry = mock<MeterRegistry> {
            on { timer(any(), any<MutableIterable<Tag>>()) } doAnswer {
                lastMeasurementName.set(it.getArgument(0))
                lastTags.set(it.getArgument(1))
                mock()
            }
        }

        val factory = StatsRegistryFactory(meterRegistry, executionContextManager, Optional.empty())
        factory.timers(measurementName, setOf(ImmutableTag("tag2", "value2")), "tag1").getMeter("value1")

        assertThat(lastMeasurementName.get()).isEqualTo(measurementName)
        assertThat(lastTags.get()).containsAnyElementsOf(expectedTags)
    }

    @Test
    fun `when utility counter creation method is used then explicit tag values are respected`() {
        val measurementName = "test"
        val expectedTags = listOf(ImmutableTag("tag1", "value1"), ImmutableTag("tag2", "value2"))
        val lastMeasurementName = AtomicReference<String>()
        val lastTags = AtomicReference<MutableIterable<Tag>>()
        val meterRegistry = mock<MeterRegistry> {
            on { counter(any(), any<MutableIterable<Tag>>()) } doAnswer {
                lastMeasurementName.set(it.getArgument(0))
                lastTags.set(it.getArgument(1))
                mock()
            }
        }

        val factory = StatsRegistryFactory(meterRegistry, executionContextManager, Optional.empty())
        factory.counters(measurementName, "tag1", "tag2").getMeter("value1", "value2")

        assertThat(lastMeasurementName.get()).isEqualTo(measurementName)
        assertThat(lastTags.get()).containsAnyElementsOf(expectedTags)
    }

    @Test
    fun `when static tags are configured for counters then they are attached to data points`() {
        val measurementName = "test"
        val expectedTags = listOf(ImmutableTag("tag1", "value1"), ImmutableTag("tag2", "value2"))
        val lastMeasurementName = AtomicReference<String>()
        val lastTags = AtomicReference<MutableIterable<Tag>>()
        val meterRegistry = mock<MeterRegistry> {
            on { counter(any(), any<MutableIterable<Tag>>()) } doAnswer {
                lastMeasurementName.set(it.getArgument(0))
                lastTags.set(it.getArgument(1))
                mock()
            }
        }

        val factory = StatsRegistryFactory(meterRegistry, executionContextManager, Optional.empty())
        factory.counters(measurementName, setOf(ImmutableTag("tag2", "value2")), "tag1").getMeter("value1")

        assertThat(lastMeasurementName.get()).isEqualTo(measurementName)
        assertThat(lastTags.get()).containsAnyElementsOf(expectedTags)
    }
}