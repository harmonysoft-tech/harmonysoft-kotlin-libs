package tech.harmonysoft.oss.micrometer.influxdb.serialization

import io.micrometer.core.instrument.*
import java.util.concurrent.TimeUnit
import javax.inject.Named

/**
 * Allows serializing given meters into InfluxDB Line Protocol format
 * (https://v2.docs.influxdata.com/v2.0/reference/syntax/line-protocol)
 */
@Named
class LineProtocolSerializer {

    /**
     * we use this cache because default influxdb publisher replaces all `.` by `_` and we don't want
     * to do that every time
     */
    private val namesCache = mutableMapOf<Meter.Id, String>()

    /**
     * We cache tags because [Tag.getConventionTags()] creates new collection every time.
     *
     * Non thread safe collection is used here in assumption that all processing is done from a single thread.
     */
    private val tagsCache = mutableMapOf<Meter.Id, Array<Tag>>()

    fun serialize(meters: Collection<Meter>, output: StringBuilder, timeUnit: TimeUnit) {
        for (meter in meters) {
            if (output.isNotEmpty()) {
                output.append("\n")
            }
            writeMeasurementAndTags(meter, output)
            when (meter) {
                is Timer -> writeTimer(meter, output, timeUnit)
                is Counter -> writeCounter(meter, output)
                is TimeGauge -> writeTimeGauge(meter, output, timeUnit)
                is Gauge -> writeGauge(meter, output)
                is DistributionSummary -> writeSummary(meter, output)
                is LongTaskTimer -> writeLongTaskTimer(meter, output, timeUnit)
                is FunctionCounter -> writeFunctionCounter(meter, output)
                is FunctionTimer -> writeFunctionTimer(meter, output, timeUnit)
                else -> writeGenericMeter(meter, output)
            }
            writeTimestamp(output)
        }
    }

    private fun writeMeasurementAndTags(meter: Meter, output: StringBuilder) {
        writeText(getName(meter.id), output)

        val tags = getTags(meter.id)
        for (tag in tags) {
            output.append(",")
            writeText(tag.key, output)
            output.append("=")
            writeText(tag.value, output)
        }
    }

    private fun getName(meterId: Meter.Id): String {
        return namesCache[meterId] ?: namesCache.computeIfAbsent(meterId) {
            meterId.name.replace('.', '_')
        }
    }

    private fun writeText(s: CharSequence, output: StringBuilder) {
        var i = 0
        val max = s.length
        while (i < max) {
            val c = s[i++]
            if (c == ' ' || c == '=' || c == ',' || c == '\"') {
                output.append('\\')
            }
            output.append(c)
        }
    }

    private fun getTags(meterId: Meter.Id): Array<Tag> {
        return tagsCache[meterId] ?: tagsCache.computeIfAbsent(meterId) {
            meterId.tags.toTypedArray()
        }
    }

    private fun writeTimer(meter: Timer, output: StringBuilder, timeUnit: TimeUnit) {
        writeMetricType(MetricType.HISTOGRAM, output)
        writeField(Field.SUM, meter.totalTime(timeUnit), output)
        writeField(Field.COUNT, meter.count(), output)
        writeField(Field.MEAN, meter.mean(timeUnit), output)
        writeField(Field.UPPER, meter.max(timeUnit), output)
    }

    private fun writeMetricType(type: String, output: StringBuilder) {
        output.append(",metric_type=")
        output.append(type)
        output.append(" ")
    }

    @Suppress("SameParameterValue")
    private fun writeField(name: String, value: Int, output: StringBuilder) {
        writeFieldCommon(name, output)
        output.append(value)
    }

    private fun writeFieldCommon(name: String, output: StringBuilder) {
        if (output.last() != ' ') {
            output.append(",")
        }
        output.append(name)
        output.append("=")
    }

    @Suppress("SameParameterValue")
    private fun writeField(name: String, value: Long, output: StringBuilder) {
        writeFieldCommon(name, output)
        output.append(value)
    }

    private fun writeField(name: String, value: Double, output: StringBuilder) {
        writeFieldCommon(name, output)
        writeDouble(value, output)
    }

    fun writeDouble(value: Double, output: StringBuilder) {
        if (!value.isFinite()) {
            output.append("NaN")
            return
        }
        output.append(value.toLong())
        var decimal = (value * PRECISION).toLong() % PRECISION
        while (decimal != 0L && decimal % 10 == 0L) {
            decimal /= 10
        }
        if (decimal != 0L) {
            output.append(".")
            // we don't do proper rounding here to keep code simple
            output.append(decimal)
        }
    }

    private fun writeTimestamp(output: StringBuilder) {
        output.append(" ")
        output.append(System.currentTimeMillis())
    }

    private fun writeCounter(meter: Counter, output: StringBuilder) {
        writeMetricType(MetricType.COUNTER, output)
        writeField(Field.VALUE, meter.count(), output)
    }

    private fun writeGauge(meter: Gauge, output: StringBuilder) {
        writeMetricType(MetricType.GAUGE, output)
        writeField(Field.VALUE, meter.value(), output)
    }

    private fun writeSummary(meter: DistributionSummary, output: StringBuilder) {
        writeMetricType(MetricType.HISTOGRAM, output)
        writeField(Field.SUM, meter.totalAmount(), output)
        writeField(Field.COUNT, meter.count(), output)
        writeField(Field.MEAN, meter.mean(), output)
        writeField(Field.UPPER, meter.max(), output)
    }

    private fun writeLongTaskTimer(meter: LongTaskTimer, output: StringBuilder, timeUnit: TimeUnit) {
        writeMetricType(MetricType.LONG_TASK_TIMER, output)
        writeField(Field.ACTIVE_TASKS, meter.activeTasks(), output)
        writeField(Field.DURATION, meter.duration(timeUnit), output)
    }

    private fun writeTimeGauge(meter: TimeGauge, output: StringBuilder, timeUnit: TimeUnit) {
        writeMetricType(MetricType.GAUGE, output)
        writeField(Field.VALUE, meter.value(timeUnit), output)
    }

    private fun writeFunctionCounter(meter: FunctionCounter, output: StringBuilder) {
        writeMetricType(MetricType.COUNTER, output)
        writeField(Field.VALUE, meter.count(), output)
    }

    private fun writeFunctionTimer(meter: FunctionTimer, output: StringBuilder, timeUnit: TimeUnit) {
        writeMetricType(MetricType.HISTOGRAM, output)
        writeField(Field.SUM, meter.totalTime(timeUnit), output)
        writeField(Field.COUNT, meter.count(), output)
        writeField(Field.MEAN, meter.mean(timeUnit), output)
    }

    private fun writeGenericMeter(meter: Meter, output: StringBuilder) {
        writeMetricType(meter.id.type.name.lowercase(), output)
        val measurements = meter.measure()
        for (measurement in measurements) {
            val value = measurement.value
            if (!value.isFinite()) {
                continue
            }
            writeField(measurement.statistic.tagValueRepresentation, value, output)
        }
    }

    companion object {
        // standard micrometer influxdb publisher uses DoubleFormat.decimalOrNan(value), which, in turn,
        // uses 6 digits precision
        const val PRECISION = 1000000L
    }

    object MetricType {
        const val COUNTER = "counter"
        const val GAUGE = "gauge"
        const val HISTOGRAM = "histogram"
        const val LONG_TASK_TIMER = "long_task_timer"
    }

    object Field {
        const val ACTIVE_TASKS = "active_tasks"
        const val COUNT = "count"
        const val DURATION = "duration"
        const val MEAN = "mean"
        const val SUM = "sum"
        const val UPPER = "upper"
        const val VALUE = "value"
    }
}