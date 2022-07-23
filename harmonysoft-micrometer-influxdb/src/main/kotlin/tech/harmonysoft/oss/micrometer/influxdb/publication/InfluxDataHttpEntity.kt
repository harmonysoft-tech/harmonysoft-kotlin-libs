package tech.harmonysoft.oss.micrometer.influxdb.publication

import org.apache.hc.core5.function.Supplier
import org.apache.hc.core5.http.Header
import org.apache.hc.core5.http.HttpEntity
import java.io.InputStream
import java.io.OutputStream

class InfluxDataHttpEntity(
    private val input: NakedByteArrayInputStream
) : HttpEntity {

    private val trailers = object : Supplier<MutableList<out Header>> {

        private val data = mutableListOf<Header>()

        override fun get(): MutableList<out Header> {
            return data
        }
    }

    private val _trailerNames = mutableSetOf<String>()

    private val buffer = ByteArray(1024)

    val encoding = Charsets.ISO_8859_1

    private val contentTypeString = "text/plain; charset=${encoding.name()}"

    override fun getContentLength(): Long {
        // org.apache.hc.client5.http.entity.GzipCompressingEntity.getContentLength() does this
        return -1
    }

    override fun writeTo(outStream: OutputStream) {
        var bytes = input.read(buffer)
        while (bytes >= 0) {
            outStream.write(buffer, 0, bytes)
            bytes = input.read(buffer)
        }
    }

    override fun getContentEncoding(): String {
        return "gzip"
    }

    override fun getContentType(): String {
        return contentTypeString
    }

    override fun getContent(): InputStream {
        return input
    }

    override fun close() {
        input.close()
    }

    override fun isChunked(): Boolean {
        return true
    }

    override fun isRepeatable(): Boolean {
        return false
    }

    override fun getTrailerNames(): MutableSet<String> {
        return _trailerNames
    }

    override fun isStreaming(): Boolean {
        return false
    }

    override fun getTrailers(): Supplier<MutableList<out Header>> {
        return trailers
    }
}