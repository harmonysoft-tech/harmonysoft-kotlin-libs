package tech.harmonysoft.oss.event.bus

interface EventBus {

    fun post(event: Any)

    fun register(subscriber: Any)
}