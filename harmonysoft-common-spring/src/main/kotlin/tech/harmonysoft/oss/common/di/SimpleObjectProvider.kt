package tech.harmonysoft.oss.common.di

import org.springframework.beans.factory.ObjectProvider

/**
 * [ObjectProvider] implementation based on the given target value to use.
 *
 * The main use-case is tests, however, it can be used during programmatic instantiation as well.
 */
class SimpleObjectProvider<T>(
    private val target: T
) : ObjectProvider<T> {

    override fun getObject(vararg args: Any?): T {
        return target
    }

    override fun getObject(): T {
        return target
    }

    override fun getIfAvailable(): T? {
        return target
    }

    override fun getIfUnique(): T? {
        return target
    }

    companion object {

        fun <T> provider(target: T): SimpleObjectProvider<T> {
            return SimpleObjectProvider(target)
        }
    }
}