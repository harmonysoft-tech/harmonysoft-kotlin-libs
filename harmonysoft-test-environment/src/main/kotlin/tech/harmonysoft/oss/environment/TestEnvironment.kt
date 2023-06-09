package tech.harmonysoft.oss.environment

import kotlin.reflect.KClass

interface TestEnvironment<CONFIG : Any> {

    val id: String

    val configClass: KClass<CONFIG>

    fun isRunning(config: CONFIG): Boolean

    fun start(context: TestContext): CONFIG
}