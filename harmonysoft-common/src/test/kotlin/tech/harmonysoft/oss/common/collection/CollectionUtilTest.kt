package tech.harmonysoft.oss.common.collection

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CollectionUtilTest {

    @Test
    fun `when map with nested maps is flattened then it works as expected`() {
        val input = mapOf(
            "key1" to "value1",
            "key2" to mapOf(
                "key3" to "value3",
                "key4" to mapOf(
                    "key5" to "value5"
                )
            )
        )
        Assertions.assertThat(CollectionUtil.flatten(input)).isEqualTo(mapOf(
            "key1" to "value1",
            "key2.key3" to "value3",
            "key2.key4.key5" to "value5"
        ))
    }

    @Test
    fun `when map with nested list with nested map is flattened then it works as expected`() {
        val input = mapOf(
            "key1" to listOf(
                "value1",
                mapOf(
                    "key2" to "value2",
                    "key3" to listOf("value3", "value4")
                )
            )
        )
        Assertions.assertThat(CollectionUtil.flatten(input)).isEqualTo(mapOf(
            "key1[0]" to "value1",
            "key1[1].key2" to "value2",
            "key1[1].key3[0]" to "value3",
            "key1[1].key3[1]" to "value4"
        ))
    }

    @Test
    fun `when map with nested list with nested map is unflattened then it works as expected`() {
        val input = mapOf(
            "key1[0]" to "value1",
            "key1[1].key2" to "value2",
            "key1[1].key3[1]" to "value4",
            "key1[1].key3[0]" to "value3"
        )
        Assertions.assertThat(CollectionUtil.unflatten(input)).isEqualTo(mapOf(
            "key1" to listOf(
                "value1",
                mapOf(
                    "key2" to "value2",
                    "key3" to listOf("value3", "value4")
                )
            )
        ))
    }

    @Test
    fun `when multi-dimensional list is unflattened then it works as expected`() {
        val input = mapOf(
            "key1[0][1]" to "value2",
            "key1[1].key2" to "value3",
            "key1[0][0].key3[0]" to "value1"
        )
        Assertions.assertThat(CollectionUtil.unflatten(input)).isEqualTo(mapOf(
            "key1" to listOf(
                listOf(
                    mapOf("key3" to listOf("value1")),
                    "value2"
                ),
                mapOf("key2" to "value3")
            )
        ))
    }

    @Test
    fun `when map with null values is unflattened then it works as expected`() {
        val input = mapOf(
            "data.key1" to "value1",
            "data.key2" to null
        )
        Assertions.assertThat(CollectionUtil.unflatten(input)).isEqualTo(mapOf(
            "data" to mapOf(
                "key1" to "value1",
                "key2" to null
            )
        ))
    }
}