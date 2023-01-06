package tech.harmonysoft.oss.slf4j.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.lang.reflect.Executable

@Configuration
open class HarmonysoftSlf4jConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    open fun logger(injectionPoint: InjectionPoint): Logger {
        val containingClass = (injectionPoint.member as? Executable)?.declaringClass
                              ?: injectionPoint.field?.declaringClass
                              ?: throw UnsupportedOperationException(
                                  "can't extract containing class from $injectionPoint"
                              )
        return LoggerFactory.getLogger(containingClass)
    }
}