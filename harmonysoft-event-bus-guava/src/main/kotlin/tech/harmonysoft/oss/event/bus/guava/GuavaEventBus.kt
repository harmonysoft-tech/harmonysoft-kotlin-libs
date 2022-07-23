package tech.harmonysoft.oss.event.bus.guava

import tech.harmonysoft.oss.event.bus.EventBus

class GuavaEventBus(
    private val delegate: com.google.common.eventbus.EventBus
) : EventBus {

    constructor() : this(com.google.common.eventbus.EventBus())

    override fun post(event: Any) {
        delegate.post(event)
    }

    override fun register(subscriber: Any) {
        delegate.register(subscriber)
    }
}