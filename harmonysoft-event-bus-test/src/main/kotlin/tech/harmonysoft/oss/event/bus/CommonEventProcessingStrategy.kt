package tech.harmonysoft.oss.event.bus

import tech.harmonysoft.oss.inpertio.client.event.ConfigChangedEvent

class CommonEventProcessingStrategy : EventProcessingStrategy {

    override fun decide(event: Any): EventProcessingType {
        return if (event is ConfigChangedEvent) {
            EventProcessingType.SYNC
        } else {
            EventProcessingType.NEUTRAL
        }
    }
}