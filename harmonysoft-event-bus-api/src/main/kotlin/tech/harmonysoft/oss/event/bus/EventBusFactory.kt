package tech.harmonysoft.oss.event.bus

interface EventBusFactory {

    fun sync(): EventBus

    fun async(): EventBus
}