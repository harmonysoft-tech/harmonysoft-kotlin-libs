package tech.harmonysoft.oss.common.di

import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import javax.annotation.Priority
import javax.inject.Named
import javax.inject.Provider

@Component
class Dependency1

@Named
class Dependency2

@Component
class Dependency3

@Named
class RegularBean(
    val publicProperty: ObjectProvider<Dependency1>,
    private val privateProperty: ObjectProvider<Dependency2>,
    param: ObjectProvider<Dependency3>
) {

    val privatePropertyExposed = privateProperty
    val constructorParameterExposed = param
}

interface CommonInterface1

@Primary
@Component
class CommonInterfaceImpl11 : CommonInterface1

@Named
class CommonInterfaceImpl12 : CommonInterface1

@Component
class CommonInterfaceUserBean1(val i: ObjectProvider<CommonInterface1>)

interface CommonInterface2

@Priority(50)
@Component
class CommonInterfaceImpl21 : CommonInterface2

@Named
class CommonInterfaceImpl22 : CommonInterface2

@Component
class CommonInterfaceUserBean2(val i: ObjectProvider<CommonInterface2>)

@Named
class ProviderBean(val prop: Provider<Dependency1>)

@ComponentScan
@Configuration
open class MyConfiguration {

    @Bean
    open fun enhancer(): ObjectProviderEnhancer {
        return ObjectProviderEnhancer()
    }
}