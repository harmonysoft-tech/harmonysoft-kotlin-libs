package tech.harmonysoft.oss.micrometer.influxdb.publication

import java.io.ByteArrayOutputStream

class NakedByteArrayOutputStream(
    initialCapacity: Int
) : ByteArrayOutputStream(initialCapacity) {

    val rawData: ByteArray
        get() = buf
}