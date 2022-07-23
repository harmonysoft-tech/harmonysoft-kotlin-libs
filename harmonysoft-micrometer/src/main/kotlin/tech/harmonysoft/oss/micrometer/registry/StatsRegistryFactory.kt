package tech.harmonysoft.oss.micrometer.registry

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import tech.harmonysoft.oss.common.execution.ExecutionContextManager
import tech.harmonysoft.oss.micrometer.auto.ContextTagsProvider
import java.util.Optional
import javax.inject.Named

@Named
class StatsRegistryFactory(
    private val registry: MeterRegistry,
    private val executionContextManager: ExecutionContextManager,
    tagsProviders: Optional<Collection<ContextTagsProvider>>
) {

    private val contextTagNames = tagsProviders.map { providers ->
        providers.flatMap { it.contextTags }.toSet()
    }.orElse(emptySet())

    fun timers(measurementName: String, vararg tagNames: String): StatsRegistry<Timer> {
        return timers(measurementName, emptySet(), *tagNames)
    }

    fun timers(
        measurementName: String,
        staticTags: Set<Tag> = emptySet(),
        vararg tagNames: String
    ): StatsRegistry<Timer> {
        return create(staticTags, *tagNames) { registry, tags ->
            registry.timer(measurementName, tags)
        }
    }

    fun counters(measurementName: String, vararg tagNames: String): StatsRegistry<Counter> {
        return counters(measurementName, emptySet(), *tagNames)
    }

    fun counters(
        measurementName: String,
        staticTags: Set<Tag> = emptySet(),
        vararg tagNames: String
    ): StatsRegistry<Counter> {
        return create(staticTags, *tagNames) { registry, tags ->
            registry.counter(measurementName, tags)
        }
    }

    fun <T : Meter> create(
        staticTags: Set<Tag> = emptySet(),
        vararg tagNames: String,
        meterCreator: (MeterRegistry, Iterable<Tag>) -> T
    ): StatsRegistry<T> {
        return StatsRegistry(
            contextTagNames = contextTagNames,
            tagNames = tagNames.toList(),
            staticTags = staticTags,
            registry = registry,
            executionContextManager = executionContextManager,
            meterCreator = meterCreator
        )
    }
}