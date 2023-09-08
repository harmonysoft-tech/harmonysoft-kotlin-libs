package tech.harmonysoft.oss.http.server.mock.response

import java.util.concurrent.atomic.AtomicInteger
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.LoggerFactory

data class CountConstrainedResponseProvider(
    private val delegate: ResponseProvider,
    private val maxCount: Int
) : ResponseProvider {

    private val count = AtomicInteger()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun maybeRespond(request: HttpRequest): HttpResponse? {
        if (count.get() >= maxCount) {
            return null
        }
        return delegate.maybeRespond(request)?.also {
            count.incrementAndGet()
            if (count.get() >= maxCount) {
                logger.info("disabling HTTP response '{}' after {} time(s)", delegate, maxCount)
            } else {
                logger.info("HTTP response '{}' used {} times out of {}", delegate, count.get(), maxCount)
            }
        }
    }

    override fun toString(): String {
        return "$delegate to return the value maximum $maxCount time(s)"
    }
}