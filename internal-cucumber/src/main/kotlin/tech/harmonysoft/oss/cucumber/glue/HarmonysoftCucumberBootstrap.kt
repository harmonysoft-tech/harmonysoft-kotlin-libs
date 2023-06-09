package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.java.Before
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import tech.harmonysoft.oss.HarmonysoftTestApplication

@CucumberContextConfiguration
@SpringBootTest(classes = [HarmonysoftTestApplication::class])
class HarmonysoftCucumberBootstrap {
    @Before
    fun bootstrap() {
    }
}