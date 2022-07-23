package tech.harmonysoft.oss.common.di

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Provider

/**
 * We quite often need to inject not a dependency object itself but a wrapper around it in order to break
 * circular dependency like `A -> B`, `B -> C`, `C -> A`.
 *
 * Many popular dependency injection frameworks like Spring do full context resolve when an actual object needs
 * to be retrieved. The benefit is that is something new is added to context later on, that is properly reflected.
 * The problem is that that is usually such process is slow.
 *
 * When we know that a provider is used as a singleton just to solve circular dependency, it's worth to cache
 * the target decorated object on the first request and reuse it for all subsequent calls. This class facilitates
 * that.
 */
class CacheableProvider<T : Any>(
    private val delegate: () -> T
) : Provider<T> {

    private val targetRef = AtomicReference<T?>()

    override fun get(): T {
        val cached = targetRef.get()
        if (cached != null) {
            return cached
        }

        return delegate().apply {
            targetRef.set(this)
        }
    }
}