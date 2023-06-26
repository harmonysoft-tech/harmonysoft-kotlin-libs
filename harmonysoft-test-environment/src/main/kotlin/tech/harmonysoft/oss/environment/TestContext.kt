package tech.harmonysoft.oss.environment

import java.io.File
import tech.harmonysoft.oss.test.util.TestUtil

data class TestContext(
    val rootDir: File,
    val executionId: String
) {

    fun prepareDirectory(path: String): File {
        val result = File(rootDir, path)
        if (result.isDirectory) {
            return result
        }
        if (result.exists()) {
            TestUtil.fail("can't create test directory ${result.canonicalPath} - there is a file at the target path")
        }
        val ok = result.mkdirs()
        if (!ok) {
            TestUtil.fail("can't create auxiliary directory ${result.canonicalPath}")
        }
        return result
    }
}