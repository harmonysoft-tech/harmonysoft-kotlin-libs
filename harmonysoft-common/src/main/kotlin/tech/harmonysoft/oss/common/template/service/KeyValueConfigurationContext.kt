package tech.harmonysoft.oss.common.template.service

import tech.harmonysoft.oss.common.data.DataModificationStrategy

/**
 * As defined in [KeyValueConfigurer] documentation, sometimes we need to configure target [DataModificationStrategy]
 * with respect to some info available in runtime. This interface defines contract for such dynamic info.
 */
interface KeyValueConfigurationContext<K> {

    /**
     * Target configuration rules might be defined as below:
     * 1. Take current runtime value for key `key1`
     * 2. If that value is equal to `baseValue1` then use `derivedValue1`
     * 3. If that value is equal to `baseValue2` then use `derivedValue2`
     * 4. Else use `derivedValue3`
     *
     * This method defines contract for obtaining base dynamic value for the given key from the given context.
     */
    fun getByStaticKey(key: K): Any?

    /**
     * We might have a finite state of possible keys (e.g. values of some enum) but want to allow flexible
     * configuration based on some custom key as well.
     *
     * Example: suppose that target [DataModificationStrategy] is a result of database select, essentially
     * a collection of column names to their row values. Keys set is finite here - all columns in that table.
     * Let's assume that we want to modify value for particular column based on some meta-info. For example,
     * use different values when we work with different database types (like oracle vs mysql). We can define
     * such config rules as below:
     *   1. If `<db-type> = oracle` then use `oracleValue`
     *   2. If `<db-type> = mysql` then use `mysqlValue`
     *   3. Else don't modify the value
     *
     * In this situation `db-type` is a custom dynamic key and target [KeyValueConfigurationContext] implementation
     * should be aware of it
     */
    fun getByDynamicKey(key: String): Any?

    companion object {

        private val EMPTY = object : KeyValueConfigurationContext<Any> {
            override fun getByStaticKey(key: Any): Any? {
                return null
            }

            override fun getByDynamicKey(key: String): Any? {
                return null
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <K> empty(): KeyValueConfigurationContext<K> {
            return EMPTY as KeyValueConfigurationContext<K>
        }

        fun <K> wrapStaticData(data: Map<K, Any?>): KeyValueConfigurationContext<K> {
            return object : KeyValueConfigurationContext<K> {
                override fun getByStaticKey(key: K): Any? {
                    return data[key]
                }

                override fun getByDynamicKey(key: String): Any? {
                    return null
                }
            }
        }

        fun <K> wrapDynamicData(data: Map<String, Any?>): KeyValueConfigurationContext<K> {
            return object : KeyValueConfigurationContext<K> {
                override fun getByStaticKey(key: K): Any? {
                    return null
                }

                override fun getByDynamicKey(key: String): Any? {
                    return data[key]
                }
            }
        }

        fun <K> wrap(
            staticKey2data: Map<K, Any?>,
            dynamicKey2data: Map<String, Any?>
        ): KeyValueConfigurationContext<K> {
            return object : KeyValueConfigurationContext<K> {
                override fun getByStaticKey(key: K): Any? {
                    return staticKey2data[key]
                }

                override fun getByDynamicKey(key: String): Any? {
                    return dynamicKey2data[key]
                }
            }
        }
    }
}