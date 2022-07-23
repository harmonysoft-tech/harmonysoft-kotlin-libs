package tech.harmonysoft.oss.test.fixture

import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.di.DiElementsComparatorConfigProvider
import tech.harmonysoft.oss.common.meta.MetaValueUtil
import tech.harmonysoft.oss.test.TestAware
import tech.harmonysoft.oss.test.fixture.meta.function.FixtureMetaFunction
import tech.harmonysoft.oss.test.fixture.meta.value.FixtureMetaValueMapper
import tech.harmonysoft.oss.test.util.TestUtil.fail
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import javax.inject.Provider

@Named
class FixtureDataHelper (
    private val allMappers: Provider<Collection<FixtureMetaValueMapper<*>>>,
    private val comparatorConfigProvider: DiElementsComparatorConfigProvider,

    enrichers: Provider<Collection<FixtureDataEnricher<*>>>,
    metaFunctions: Provider<Collection<FixtureMetaFunction>>
) : TestAware {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val excludedMetaValues = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    @Suppress("UNCHECKED_CAST")
    private val mappersByType: Map<FixtureType<Any>, Collection<FixtureMetaValueMapper<Any>>> by lazy {
        allMappers.get().map { it as FixtureMetaValueMapper<Any> }.groupBy { it.type }
    }

    @Suppress("UNCHECKED_CAST")
    private val enrichersByType: Map<FixtureType<Any>, List<FixtureDataEnricher<Any>>> by lazy {
        enrichers.get().map { it as FixtureDataEnricher<Any> }.groupBy { it.type }.mapValues {
            it.value.sortedWith(comparatorConfigProvider.data)
        }
    }

    private val functionsByName: Map<String, FixtureMetaFunction> by lazy {
        val functions = metaFunctions.get().groupBy { it.functionName }
        val duplicateFunctions = functions.filter {
            it.value.size > 1
        }
        if (duplicateFunctions.isNotEmpty()) {
            fail("duplicate meta functions - " + duplicateFunctions.entries.sortedBy { it.key }.joinToString {
                "${it.key}: ${it.value.joinToString { impl -> impl.javaClass.name }}"
            })
        }
        functions.mapValues { it.value.first() }
    }

    override fun onTestEnd() {
        excludedMetaValues.clear()
    }

    fun excludeMetaValueFromExpansion(metaValue: String) {
        excludedMetaValues += metaValue
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> maybeExpandMetaValues(type: FixtureType<T>, context: T, input: String): String? {
        val metaValues = MetaValueUtil.extractMetaValues(input) - excludedMetaValues
        if (metaValues.isEmpty()) {
            return input
        }

        val mappers =
            (mappersByType[type as FixtureType<Any>]?.sortedWith(comparatorConfigProvider.data) ?: emptyList()) +
            allMappers.get().filter {
                it.type != type && it.type.fixtureContextClass.isInstance(context)
            }.map { it as FixtureMetaValueMapper<Any> }.sortedWith(comparatorConfigProvider.data)
        if (mappers.isEmpty()) {
            logger.info("No expansion mappers are available for meta-value config keys {}, skipping their expansion",
                        metaValues.joinToString { "<$it>" })
            return input
        }

        return metaValues.fold(input) { currentInput, metaValue ->
            val remappedResult = mappers.map {
                it.map(context as Any, metaValue)
            }.firstOrNull { it.success }
            if (remappedResult != null && remappedResult.successValue == null) {
                return null
            }

            val remapped = remappedResult?.successValue ?: maybeExpandFunctions(metaValue)
            if (remapped == null) {
                logger.info("No expansion rules are available for meta-value '{}', skipping its expansion",
                            metaValue)
                currentInput
            } else {
                logger.info("Expanding meta-value <{}> as '{}'", metaValue, remapped)
                currentInput.replace("<$metaValue", remapped)
            }
        }
    }

    fun maybeExpandFunctions(input: String): String? {
        return FUNCTION_PATTERN.matchEntire(input)?.let {
            if (it.groupValues.size == 3) {
                functionsByName[it.groupValues[1]]?.applyFunction(it.groupValues[2]) ?: fail(
                    "no meta-function implementation with name '${it.groupValues[1]}' was found. " +
                    "Available functions: ${functionsByName.keys}"
                )
            } else {
                fail("input '$input' was recognized by function pattern but not parsed correctly. Extracted "
                     + "regex groups: ${it.groupValues}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> enrichTestData(type: FixtureType<T>, context: T, data: Map<String, String?>): Map<String, String?> {
        return enrichersByType[type as FixtureType<Any>]?.fold(data) { dataToUse, enricher ->
            enricher.enrich(context as Any, dataToUse)
        } ?: data
    }

    fun <T : Any> prepareTestData(
        type: FixtureType<T>,
        context: T,
        data: Collection<Map<String, String>>
    ): Collection<Map<String, String?>> {
        return data.map { prepareTestData(type, context, it) }
    }

    fun <T : Any> prepareTestData(type: FixtureType<T>, context: T, data: Map<String, String>): Map<String, String?> {
        return enrichTestData(type, context, data.mapValues { maybeExpandMetaValues(type, context, it.value) })
    }

    fun <T : Any> prepareTestData(type: FixtureType<T>, context: T, data: String): String {
        val key = "temp"
        val prepared = prepareTestData(type, context, mapOf(key to data))
        return prepared[key] ?: fail("magic: $prepared")
    }

    companion object {

        val FUNCTION_PATTERN = """\s*([^(\s]+)\s*\(\s*([^)]+)\s*\)""".toRegex()
    }
}