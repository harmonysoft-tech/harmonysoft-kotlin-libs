package tech.harmonysoft.oss.event.bus

interface EventProcessingStrategy {

    fun decide(event: Any): EventProcessingType
}