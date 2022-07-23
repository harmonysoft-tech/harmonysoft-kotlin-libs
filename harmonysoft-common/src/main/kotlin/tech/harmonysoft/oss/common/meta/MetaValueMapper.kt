package tech.harmonysoft.oss.common.meta

import tech.harmonysoft.oss.common.ProcessingResult

interface MetaValueMapper {

    /**
     * Tries to map given meta-value to a new one, if it's defined as, say, `<user-id>` in configs, this method
     * is expected to receive `user-id` (without brackets)
     */
    fun map(metaValue: String): ProcessingResult<String?, Unit>
}