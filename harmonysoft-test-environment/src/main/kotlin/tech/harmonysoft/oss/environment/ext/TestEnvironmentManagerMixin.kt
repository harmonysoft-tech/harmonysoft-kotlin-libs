package tech.harmonysoft.oss.environment.ext

interface TestEnvironmentManagerMixin {

    fun beforeStart()

    object NoOp : TestEnvironmentManagerMixin {
        override fun beforeStart() {
        }
    }
}