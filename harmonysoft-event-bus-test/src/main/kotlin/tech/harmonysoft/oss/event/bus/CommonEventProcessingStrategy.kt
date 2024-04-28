package tech.harmonysoft.oss.event.bus

import tech.harmonysoft.oss.configurario.client.event.ConfigChangedEvent

class CommonEventProcessingStrategy : EventProcessingStrategy {

    override fun decide(event: Any): EventProcessingType {
        return if (event is ConfigChangedEvent) {
            EventProcessingType.SYNC
        } else {
            EventProcessingType.NEUTRAL
        }
    }
}