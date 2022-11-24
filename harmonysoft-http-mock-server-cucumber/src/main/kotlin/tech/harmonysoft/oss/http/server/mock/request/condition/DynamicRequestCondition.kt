package tech.harmonysoft.oss.http.server.mock.request.condition

import org.mockserver.model.HttpRequest

fun interface DynamicRequestCondition {

    fun matches(request: HttpRequest): Boolean

    fun and(condition: DynamicRequestCondition): DynamicRequestCondition {
        val original = this
        return object : DynamicRequestCondition {

            override fun matches(request: HttpRequest): Boolean {
                return original.matches(request) && condition.matches(request)
            }

            override fun toString(): String {
                return "$original and $condition"
            }
        }
    }

    companion object {

        val MATCH_ALL = object : DynamicRequestCondition {

            override fun matches(request: HttpRequest): Boolean {
                return true
            }

            override fun toString(): String {
                return "<match-all>"
            }
        }
    }
}