package tech.harmonysoft.oss.micrometer.influxdb

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.step.StepMeterRegistry
import org.slf4j.LoggerFactory
import tech.harmonysoft.oss.common.schedule.scheduleWithFixedDelay
import tech.harmonysoft.oss.common.time.util.TimeUtil
import tech.harmonysoft.oss.micrometer.influxdb.config.InfluxDbStatsConfig
import tech.harmonysoft.oss.micrometer.influxdb.config.InfluxDbStatsConfigProvider
import tech.harmonysoft.oss.micrometer.influxdb.publication.InfluxDbDataPublisher
import tech.harmonysoft.oss.micrometer.influxdb.serialization.LineProtocolSerializer
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Named
import javax.inject.Provider

/**
 * Profiling shows that big amount of memory is consumed by default micrometer InfluxDB publisher.
 *
 * It creates thousands of strings and other auxiliary objects during publishing.
 *
 * Out goal is to allocation expensive objects/buffers on application startup and reuse them later
 * for all subsequent stats data publishing iterations.
 *
 * Data format logic in this case is obtained from default micrometer InfluxDB
 * publisher - `io.micrometer.influx.InfluxMeterRegistry`
 */
@Named
class InfluxDbMeterRegistry(
    private val influxConfigProvider: InfluxDbStatsConfigProvider,
    private val serializer: LineProtocolSerializer,
    private val publisher: Provider<InfluxDbDataPublisher>,

    threadPool: ScheduledExecutorService
) : StepMeterRegistry(influxConfigProvider, Clock.SYSTEM) {

    private val logger = LoggerFactory.getLogger(InfluxDbMeterRegistry::class.java)

    private val lock = ReentrantLock()

    private val buffer = StringBuilder(1 shl 21)

    init {
        threadPool.scheduleWithFixedDelay(
            startImmediately = false,
            delayProvider = { TimeUtil.Millis.MINUTE },
            timeUnitProvider = { TimeUnit.MILLISECONDS },
            command = this::publish
        )

        gauge("stats.influxdb.publish.buffer.chars", buffer) {
            it.capacity().toDouble()
        }
    }

    override fun publish() {
        val success = lock.tryLock()
        if (success) {
            try {
                doPublish()
            } finally {
                lock.unlock()
            }
        } else {
            logger.warn(
                "Detected concurrent request to publish stats to InfluxDB, that should never happen. "
                + "Skipping stats publishing",
                IllegalStateException()
            )
        }
    }

    private fun doPublish() {
        val config = influxConfigProvider.data
        if (config is InfluxDbStatsConfig.Enabled) {
            buffer.clear()
            serializer.serialize(meters, buffer, baseTimeUnit)
            publisher.get().publish(buffer)
        }
    }

    override fun getBaseTimeUnit(): TimeUnit {
        return TimeUnit.MILLISECONDS
    }
}