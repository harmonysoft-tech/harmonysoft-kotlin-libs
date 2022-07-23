package tech.harmonysoft.oss.sql.dsl.target

data class SelectTarget(
    val target: SqlTarget,
    val alias: String?
) {

    constructor(target: SqlTarget) : this(target = target, alias = null)

    fun replaceColumns(columns: Map<String, String>): SelectTarget {
        val newTarget = target.replaceColumns(columns)
        return if (newTarget == target) {
            this
        } else {
            copy(target = newTarget)
        }
    }

    override fun toString(): String {
        return buildString {
            append(target)
            alias?.let {
                append(" ").append(it)
            }
        }
    }
}