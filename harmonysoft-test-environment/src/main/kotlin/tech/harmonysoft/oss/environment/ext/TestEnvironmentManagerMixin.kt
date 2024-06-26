package tech.harmonysoft.oss.environment.ext

import tech.harmonysoft.oss.environment.TestContext

interface TestEnvironmentManagerMixin {

    fun beforeStart(context: TestContext)

    fun afterStart(context: TestContext)

    fun beforeStop(context: TestContext)

    object NoOp : TestEnvironmentManagerMixin {
        override fun beforeStart(context: TestContext) {
        }

        override fun afterStart(context: TestContext) {
        }

        override fun beforeStop(context: TestContext) {
        }
    }
}