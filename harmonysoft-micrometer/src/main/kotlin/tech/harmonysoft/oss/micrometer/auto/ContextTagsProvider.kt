package tech.harmonysoft.oss.micrometer.auto

import tech.harmonysoft.oss.common.execution.ExecutionContextManager

interface ContextTagsProvider {

    /**
     * A set of tags which might be available in the current context.
     *
     * This interface is a bridge between library stats collection and end application (library user).
     *
     * All tags exposed by the current property and used in stats publishing (the values are picked up
     * from [ExecutionContextManager] during stats data point creation)
     */
    val contextTags: Set<String>
}