package tech.harmonysoft.oss.common.collection

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MapUtilTest {

    @Test
    fun `when map with nested maps is flattened than it works as expected`() {
        val input = mapOf(
            "key1" to "value1",
            "key2" to mapOf(
                "key3" to "value3",
                "key4" to mapOf(
                    "key5" to "value5"
                )
            )
        )
        assertThat(MapUtil.flatten(input)).isEqualTo(mapOf(
            "key1" to "value1",
            "key2.key3" to "value3",
            "key2.key4.key5" to "value5"
        ))
    }
}