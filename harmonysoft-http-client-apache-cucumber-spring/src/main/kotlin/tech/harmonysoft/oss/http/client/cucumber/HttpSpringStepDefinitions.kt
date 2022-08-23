package tech.harmonysoft.oss.http.client.cucumber

import io.cucumber.java.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort

class HttpSpringStepDefinitions {

    @Autowired private lateinit var webPortProvider: SpringDefaultWebPortProvider

    @Before
    fun dummy() {
        // dummy method for this class to be recognized as step definitions by cucumber
    }

    // @LocalServerPort is populated after context creation, that's why we need to initialize it lazily
    @LocalServerPort
    fun onServerPort(port: Int) {
        webPortProvider.port = port
    }
}