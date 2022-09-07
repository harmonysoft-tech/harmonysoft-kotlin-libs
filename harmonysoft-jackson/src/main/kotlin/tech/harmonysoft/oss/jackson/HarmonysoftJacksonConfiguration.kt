package tech.harmonysoft.oss.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
open class HarmonysoftJacksonConfiguration {

    @Json
    @Bean
    open fun jsonObjectMapper(extensions: Optional<Collection<ObjectMapperConfigurationExtension>>): ObjectMapper {
        return ObjectMapper().apply {
            configure(this, extensions.orElse(emptyList()))
        }
    }

    private fun configure(mapper: ObjectMapper, extensions: Collection<ObjectMapperConfigurationExtension>) {
        mapper.registerModules(KotlinModule.Builder().build(), KClassModule, JavaTimeModule())
        for (extension in extensions) {
            extension.configure(mapper)
        }
    }
}