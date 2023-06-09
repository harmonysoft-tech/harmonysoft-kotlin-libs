package tech.harmonysoft.oss.environment

interface EnvironmentInfoProvider {

    /**
     * Current test execution id, relevant in CI environment in order to have separate environments
     * for separate test suite runs (even against the same VCS tag)
     */
    val executionId: String

    object Default : EnvironmentInfoProvider {
        override val executionId = "local"
    }
}