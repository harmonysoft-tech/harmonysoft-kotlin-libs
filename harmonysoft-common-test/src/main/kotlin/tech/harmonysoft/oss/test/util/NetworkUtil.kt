package tech.harmonysoft.oss.test.util

import java.net.ServerSocket

object NetworkUtil {

    val freePort: Int
        get() {
            val socket = ServerSocket(0)
            return socket.localPort.apply {
                socket.close()
            }
        }
}