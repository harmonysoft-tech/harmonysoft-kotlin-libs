package tech.harmonysoft.oss.micrometer.influxdb.publication

import java.io.ByteArrayInputStream

class NakedByteArrayInputStream : ByteArrayInputStream(ByteArray(0)) {

    val size: Long
        get() = count.toLong()

    fun updateState(data: ByteArray, size: Int) {
        buf = data
        pos = 0
        count = size
    }
}