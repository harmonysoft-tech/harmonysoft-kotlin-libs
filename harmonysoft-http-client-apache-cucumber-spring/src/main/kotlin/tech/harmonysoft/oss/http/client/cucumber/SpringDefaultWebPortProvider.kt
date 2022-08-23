package tech.harmonysoft.oss.http.client.cucumber

import org.junit.jupiter.api.fail
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Named

@Named
class SpringDefaultWebPortProvider : DefaultWebPortProvider {

    private val _port = AtomicInteger()

    override var port: Int
        get() {
            return _port.get().takeIf { it != 0 } ?: fail("web port is not initialized")
        }
        set(value) {
            _port.set(value)
        }
}