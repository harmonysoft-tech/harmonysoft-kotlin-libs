package tech.harmonysoft.oss.http.server.mock.response

import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.LoggerFactory

class DelayedResponseProvider(
    private val delegate: ResponseProvider,
    private val delayMs: Long
) : ResponseProvider {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun maybeRespond(request: HttpRequest): HttpResponse? {
        return delegate.maybeRespond(request)?.also {
            logger.info("delaying HTTP response '{}' by {} ms", delegate, delayMs)
            Thread.sleep(delayMs)
            logger.info("finished delaying HTTP response '{}' by {} ms", delegate, delayMs)
        }
    }
}