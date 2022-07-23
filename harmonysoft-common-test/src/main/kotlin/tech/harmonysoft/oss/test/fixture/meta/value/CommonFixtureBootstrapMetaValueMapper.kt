package tech.harmonysoft.oss.test.fixture.meta.value

import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.ProcessingResult.Companion.failure
import tech.harmonysoft.oss.common.ProcessingResult.Companion.success
import tech.harmonysoft.oss.common.host.HostInfo
import tech.harmonysoft.oss.common.meta.MetaValueMapper
import tech.harmonysoft.oss.test.fixture.CommonTestFixture
import java.net.ServerSocket
import java.nio.file.Files

class CommonFixtureBootstrapMetaValueMapper(
    private val hostInfo: HostInfo
) : FixtureMetaValueMapper<Any>, MetaValueMapper {

    override val type = CommonTestFixture.TYPE

    override fun map(context: Any, metaValue: String): ProcessingResult<String?, Unit> {
        return map(CommonTestFixture.TYPE, metaValue)
    }

    override fun map(metaValue: String): ProcessingResult<String?, Unit> {
        return when (metaValue) {
            "random-dir-path" -> success(Files.createTempDirectory("").toFile().apply {
                deleteOnExit()
            }.absolutePath)
            "empty-string" -> success("")
            "null" -> success(null)
            "free-port" -> {
                val socket = ServerSocket(0)
                val port = socket.localPort
                socket.close()
                success(port.toString())
            }
            "current-host" -> success(hostInfo.hostName)
            "current-host-short-name" -> success(hostInfo.shortHostName)
            else -> failure(Unit)
        }
    }
}