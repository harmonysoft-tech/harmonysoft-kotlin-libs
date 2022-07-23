package tech.harmonysoft.oss.test.content

interface TestContentManager {

    fun getContent(name: String): ByteArray

    fun setContent(name: String, data: ByteArray)
}