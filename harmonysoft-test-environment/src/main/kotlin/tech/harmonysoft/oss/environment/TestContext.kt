package tech.harmonysoft.oss.environment

import java.io.File

data class TestContext(
    val rootDir: File,
    val executionId: String
)