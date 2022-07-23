package tech.harmonysoft.oss.event.bus.spring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import tech.harmonysoft.oss.event.bus.AutoSubscribe
import tech.harmonysoft.oss.event.bus.EventBus
import javax.inject.Named
import kotlin.reflect.full.findAnnotation

@Named
class EventBusAutoSubscriber(
    private val eventBus: EventBus,
    @Value("\${$ROOT_PACKAGES_PROPERTY:}") rawRootPackages: String
) : BeanPostProcessor {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootPackages: Set<String>

    init {
        rootPackages = if (rawRootPackages.isBlank()) {
            logger.info("Skipped event bus auto-subscription - no root packages are defined via property '{}'",
                        ROOT_PACKAGES_PROPERTY)
            emptySet()
        } else {
            rawRootPackages.split(",").map(String::trim).toSet().apply {
                logger.info("Enabling auto event bus subscription for {} root package(s): {}", size, joinToString())
            }
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        return super.postProcessAfterInitialization(bean, beanName)?.also { beanToUse ->
            beanToUse.takeIf {
                rootPackages.any {
                    beanToUse::class.qualifiedName?.startsWith(it) == true
                }
            }?.let {
                val shouldSubscribe = it::class.members.any { it.findAnnotation<AutoSubscribe>() != null }
                if (shouldSubscribe) {
                    eventBus.register(it)
                    logger.info("Automatically subscribed bean '{}' to event bus", beanName)
                }
            }
        }
    }

    companion object {
        const val ROOT_PACKAGES_PROPERTY = "harmonysoft.event.auto.subscription.root.packages"
    }
}