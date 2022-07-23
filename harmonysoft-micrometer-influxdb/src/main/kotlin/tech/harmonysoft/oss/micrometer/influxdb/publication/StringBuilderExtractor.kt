package tech.harmonysoft.oss.micrometer.influxdb.publication

import java.io.OutputStream
import java.io.Writer
import java.lang.reflect.Field
import java.nio.charset.Charset
import java.util.zip.GZIPOutputStream

/**
 * Supports reflection based data extraction from [StringBuilder] for both java 8 and java 9+
 */
sealed class StringBuilderExtractor {

    /**
     * Extracts data from the given input buffer, compresses it with gzip and writes to the given output
     */
    fun writeDataToStream(data: StringBuilder, charset: Charset, outStream: OutputStream) {
        GZIPOutputStream(outStream).use { gzipOut ->
            gzipOut.writer(charset).use { writer ->
                writeData(data, writer)
            }
        }
    }

    protected abstract fun writeData(data: StringBuilder, writer: Writer)

    private class CharArray(
        val dataField: Field
    ) : StringBuilderExtractor() {

        override fun writeData(data: StringBuilder, writer: Writer) {
            // in java 8 and below accessing the char array directly is the most effective
            val dataArray = dataField.get(data) as kotlin.CharArray
            writer.write(dataArray, 0, data.length)
        }
    }

    private object ByteArray : StringBuilderExtractor() {
        override fun writeData(data: StringBuilder, writer: Writer) {
            // in java 9+ String implementation changed to byte[]; generally a character can be either single or
            // double byte and we need the builder to interpret the characters correctly
            data.asIterable().forEach { c ->
                writer.write(c.code)
            }
        }
    }

    companion object {

        val extractor: StringBuilderExtractor
            get() {
                val fieldName = "value"
                val dataField = Class.forName("java.lang.AbstractStringBuilder")
                    .getDeclaredField(fieldName).apply {
                        isAccessible = true
                    }
                return when (dataField.type) {
                    // java 8
                    kotlin.CharArray::class.java -> CharArray(dataField)
                    kotlin.ByteArray::class.java -> ByteArray
                    else -> throw UnsupportedOperationException(
                        "${StringBuilder::class.simpleName} internal '$fieldName' field is neither char array (java 8) "
                        + "nor byte array (java 9+)"
                    )
                }
            }
    }
}