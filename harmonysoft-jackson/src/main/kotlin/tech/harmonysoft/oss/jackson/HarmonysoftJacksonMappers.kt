package tech.harmonysoft.oss.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.util.Optional
import jakarta.inject.Named

/**
 * We expose the mappers of properties of this wrapper class instead of putting them directly into spring
 * context with annotations [Json] and [Yaml] because that causes problem in some spring boot auto-configurations.
 * For example, `org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration$JacksonCodecConfiguration`
 * expects to get a single [ObjectMapper] instance and fails with an exception as below if the both
 * mappers are registered in the spring context:
 *
 * ```
 * Parameter 0 of method jacksonCodecCustomizer in org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration$JacksonCodecConfiguration required a single bean, but 2 were found:
 * - jsonObjectMapper: defined by method 'jsonObjectMapper' in class path resource [tech/harmonysoft/oss/jackson/HarmonysoftJacksonConfiguration.class]
 * - yamlObjectMapper: defined by method 'yamlObjectMapper' in class path resource [tech/harmonysoft/oss/jackson/HarmonysoftJacksonConfiguration.class]
 * ```
 */
@Named
class HarmonysoftJacksonMappers(
    extensions: Optional<Collection<ObjectMapperConfigurationExtension>>
) {

    val json = ObjectMapper().apply {
        configure(this, extensions.orElse(emptyList()))
    }

    val yaml = ObjectMapper(YAMLFactory()).apply {
        configure(this, extensions.orElse(emptyList()))
    }

    private fun configure(mapper: ObjectMapper, extensions: Collection<ObjectMapperConfigurationExtension>) {
        mapper.registerModules(KotlinModule.Builder().build(), KClassModule, JavaTimeModule())
        for (extension in extensions) {
            extension.configure(mapper)
        }
    }
}