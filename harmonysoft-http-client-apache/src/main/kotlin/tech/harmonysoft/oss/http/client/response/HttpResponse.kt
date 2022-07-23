package tech.harmonysoft.oss.http.client.response

data class HttpResponse<T>(
    val status: Int,
    val statusText: String,
    val body: T,
    val headers: Map<String, String>
) {

    fun <T> withBody(body: T): HttpResponse<T> {
        return HttpResponse(
            status = status,
            statusText = statusText,
            body = body,
            headers = headers
        )
    }

    override fun toString(): String {
        return "$status $statusText"
    }
}