package tech.harmonysoft.oss.common.di

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import tech.harmonysoft.oss.common.string.util.isNotNullNotBlankEffective
import java.lang.reflect.ParameterizedType
import java.util.*
import javax.inject.Named
import javax.inject.Provider

/**
 * Binds [CacheableObjectProvider] to [ObjectProvider] and [CacheableProvider] to [Provider]
 */
@Named
class ObjectProviderEnhancer : BeanFactoryPostProcessor {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        if (System.getProperty("harmonysoft.di.spring.cache.provider.enabled") == "false") {
            logger.info("Skipped replacing dependency provides by cacheable implementation")
            return
        }
        for (beanName in beanFactory.beanDefinitionNames) {
            val definition = beanFactory.getBeanDefinition(beanName)
            if (definition.factoryMethodName.isNotNullNotBlankEffective()) {
                // we assume that providers are injected instead of beans themselves only when we want to solve
                // circular dependencies, that's why they shouldn't be used as factory method arguments,
                // so, for simplicity we don't handle such use-case here
                continue
            }

            val beanClassName = definition.beanClassName ?: continue
            val beanClass = Class.forName(beanClassName)
            val constructors = beanClass.constructors
            for (constructor in constructors) {
                val parameterTypes = constructor.genericParameterTypes
                var i = -1
                for (parameterType in parameterTypes) {
                    ++i
                    if (parameterType !is ParameterizedType) {
                        continue
                    }
                    val rawClass = parameterType.rawType as? Class<*> ?: continue
                    if (rawClass != ObjectProvider::class.java && rawClass != Provider::class.java) {
                        continue
                    }
                    val typeArguments = parameterType.actualTypeArguments
                    if (typeArguments.size != 1) {
                        logger.warn(
                            "Expected to find a single type parameter in class {} but found {}: {}, skipping "
                            + "enhancing for it", rawClass.name, typeArguments.size, typeArguments
                        )
                        break
                    }
                    val typeArgument = typeArguments.first()
                    if (typeArgument !is Class<*>) {
                        if (typeArgument is ParameterizedType
                            && (typeArgument.rawType == Optional::class.java
                                || typeArgument.rawType == Collection::class.java
                                || typeArgument.rawType == List::class.java)
                        ) {
                            logger.info("Skipped $rawClass<${typeArgument.rawType}> enhancing for class {}",
                                        beanClassName)
                        } else {
                            logger.warn(
                                "Failed to replace {} constructor argument #{} (zero-based) by cacheable "
                                + "implementation in class {} - can't deduce type parameter value",
                                rawClass.name, i, beanClassName
                            )
                        }
                        continue
                    }
                    val objectProvider = CacheableObjectProvider(beanFactory, typeArgument)
                    val decorator: Any = if (rawClass == ObjectProvider::class.java) {
                        objectProvider
                    } else {
                        CacheableProvider {
                            objectProvider.getObject()
                        }
                    }
                    definition.constructorArgumentValues.addIndexedArgumentValue(
                        i,
                        decorator
                    )
                }
            }
        }
    }
}