package tech.harmonysoft.oss.common.meta

import org.slf4j.LoggerFactory

object MetaValueUtil {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val DYNAMIC_VALUE_PATTERN = """<([^>]++)>""".toRegex()

    /**
     * Extracts meta values from the given string.
     *
     * Example:
     *   * input: `<date>-<instance>.zip`
     *   * output: [date, instance]
     */
    fun extractMetaValues(input: String): Set<String> {
        return DYNAMIC_VALUE_PATTERN.findAll(input).map {
            it.groupValues[1].trim()
        }.toSet()
    }

    fun expand(raw: String, mappers: Collection<MetaValueMapper>): Any? {
        return expand(raw, mappers, FailedMetaValueExpansionCallback.LOG_INFO)
    }

    fun expand(raw: String, mappers: Collection<MetaValueMapper>, fallback: FailedMetaValueExpansionCallback): Any? {
        val metaValues = extractMetaValues(raw)
        if (metaValues.isEmpty()) {
            return raw
        }

        return metaValues.fold(raw as Any) { result, metaValue ->
            val remapped = mappers.map {
                it.map(metaValue)
            }.firstOrNull { it.success }

            if (remapped == null || !remapped.success) {
                fallback.onFailedExpansion(metaValue, raw)
            } else {
                remapped.successValue?.let {
                    logger.info("expanding meta-value <{}> as '{}'", metaValue, it)
                    if (result == "<$metaValue>") {
                        it
                    } else {
                        result.toString().replace("<$metaValue>", it.toString())
                    }
                } ?: return null
            }
        }
    }

    fun interface FailedMetaValueExpansionCallback {

        fun onFailedExpansion(metaValue: String, completeString: String): String

        companion object {

            private val logger = LoggerFactory.getLogger(this::class.java)

            val LOG_INFO = FailedMetaValueExpansionCallback { metaValue, completeString ->
                logger.info("failed to expand meta value '{}' in value '{}'", metaValue, completeString)
                completeString
            }
        }
    }
}