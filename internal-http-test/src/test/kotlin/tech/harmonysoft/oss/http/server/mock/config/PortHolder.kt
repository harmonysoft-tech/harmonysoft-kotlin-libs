package tech.harmonysoft.oss.http.server.mock.config

import tech.harmonysoft.oss.test.util.NetworkUtil
import javax.inject.Named

@Named
class PortHolder {

    val port = NetworkUtil.freePort
}