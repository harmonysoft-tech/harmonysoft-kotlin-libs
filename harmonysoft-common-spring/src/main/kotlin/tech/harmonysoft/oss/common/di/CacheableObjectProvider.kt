package tech.harmonysoft.oss.common.di

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.springframework.core.annotation.AnnotationUtils
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.Priority

/**
 * Solves the same problem as [CacheableProvider] but for Spring framework API
 */
class CacheableObjectProvider<T : Any>(
    private val beanFactory: ListableBeanFactory,
    private val targetBeanClass: Class<T>
) : ObjectProvider<T> {

    private val targetBeanRef = AtomicReference<T?>()

    @Suppress("UNCHECKED_CAST")
    override fun getIfAvailable(): T? {
        val cached = targetBeanRef.get()
        if (cached != null) {
            return cached
        }

        val candidates = beanFactory.getBeansOfType(targetBeanClass)
        if (candidates.size == 1) {
            // we're not concerned in concurrent calls here because the same bean instance is expected to be
            // returned from the delegate
            val result = candidates.values.first()
            targetBeanRef.set(result as T)
            return result
        }

        if (candidates.isEmpty()) {
            return null
        }

        val primary = candidates.values.filter {
            AnnotationUtils.findAnnotation(it::class.java, Primary::class.java) != null
        }
        if (primary.size == 1) {
            return (primary.first() as T).apply {
                targetBeanRef.set(this)
            }
        }

        val hasWithPriority = candidates.values.any {
            AnnotationUtils.findAnnotation(it::class.java, Priority::class.java) != null
        }
        if (!hasWithPriority) {
            throw IllegalStateException(
                "can't initialize an instance of ${targetBeanClass.name} - there are ${candidates.size} candidates, " +
                "none of them is market by ${Primary::class.qualifiedName} and or ${Priority::class.qualifiedName} - "
                + candidates.entries.joinToString { "${it.key}=${it.value::class.qualifiedName}" }
            )
        }
        return candidates.values.toList().sortedWith(AnnotationAwareOrderComparator.INSTANCE).first().apply {
            targetBeanRef.set(this)
        }
    }

    override fun getObject(vararg args: Any?): T {
        return getObject()
    }

    override fun getObject(): T {
        return ifAvailable ?: throw IllegalStateException("can't initialize target instance")
    }

    override fun getIfUnique(): T? {
        return ifAvailable
    }
}