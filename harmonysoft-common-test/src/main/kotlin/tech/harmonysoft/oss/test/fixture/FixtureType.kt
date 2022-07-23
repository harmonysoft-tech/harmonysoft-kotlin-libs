package tech.harmonysoft.oss.test.fixture

import kotlin.reflect.KClass

/**
 * Application/library specific fixture data callbacks (like )
 */
data class FixtureType<T : Any>(
    val id: String,
    val fixtureContextClass: KClass<T>
) {
    override fun toString(): String {
        return id
    }
}