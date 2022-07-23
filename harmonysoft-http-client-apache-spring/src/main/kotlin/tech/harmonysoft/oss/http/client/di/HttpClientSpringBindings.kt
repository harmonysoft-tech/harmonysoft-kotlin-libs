package tech.harmonysoft.oss.http.client.di

import org.springframework.beans.factory.DisposableBean
import tech.harmonysoft.oss.http.client.HttpClient
import javax.inject.Named

@Named
class HttpClientSpringBindings(
    private val client: HttpClient
) : DisposableBean {

    override fun destroy() {
        client.close()
    }
}