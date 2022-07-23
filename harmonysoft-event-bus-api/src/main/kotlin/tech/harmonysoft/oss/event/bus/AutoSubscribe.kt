package tech.harmonysoft.oss.event.bus

/**
 * This annotation can be applied to a method, classes which objects are included into DI context are automatically
 * subscribed to [EventBus] then
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoSubscribe
