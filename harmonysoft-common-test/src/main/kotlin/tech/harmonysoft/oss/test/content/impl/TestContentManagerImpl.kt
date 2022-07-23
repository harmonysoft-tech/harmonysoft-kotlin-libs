package tech.harmonysoft.oss.test.content.impl

import tech.harmonysoft.oss.test.TestAware
import tech.harmonysoft.oss.test.content.TestContentManager
import tech.harmonysoft.oss.test.util.TestUtil.fail
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named

@Named
class TestContentManagerImpl : TestContentManager, TestAware {

    private val content = ConcurrentHashMap<String, ByteArray>()

    override fun getContent(name: String): ByteArray {
        return content[name] ?: fail("no test content with name '$name' is found, available: ${content.keys}")
    }

    override fun setContent(name: String, data: ByteArray) {
        content[name] = data
    }

    override fun onTestEnd() {
        content.clear()
    }
}