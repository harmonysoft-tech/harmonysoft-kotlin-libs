package tech.harmonysoft.oss.event.bus.guava

import com.google.common.eventbus.AsyncEventBus
import tech.harmonysoft.oss.event.bus.EventBus
import tech.harmonysoft.oss.event.bus.EventBusFactory
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Named

@Named
class GuavaEventBusFactory(
    private val threadPool: Executor
) : EventBusFactory {

    override fun sync(): EventBus {
        return GuavaEventBus(com.google.common.eventbus.EventBus("sync-event-bus-${UUID.randomUUID()}"))
    }

    override fun async(): EventBus {
        return GuavaEventBus(AsyncEventBus("async-event-bus-${UUID.randomUUID()}", threadPool))
    }
}