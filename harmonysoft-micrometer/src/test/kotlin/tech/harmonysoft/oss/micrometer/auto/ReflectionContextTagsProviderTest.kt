package tech.harmonysoft.oss.micrometer.auto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tech.harmonysoft.oss.micrometer.auto.ReflectionContextTagsProviderTest.SampleContextKeys.FIRST
import tech.harmonysoft.oss.micrometer.auto.ReflectionContextTagsProviderTest.SampleContextKeys.SECOND

internal class ReflectionContextTagsProviderTest {

    object SampleContextKeys {
        const val FIRST = "first"
        const val SECOND = "second"
    }

    @Test
    fun `when properties are picked up through reflection then that works correctly`() {
        assertThat(ReflectionContextTagsProvider(SampleContextKeys::class).contextTags).containsOnly(FIRST, SECOND)
    }

    @Test
    fun `when a property is excluded then it is not exposed`() {
        assertThat(ReflectionContextTagsProvider(SampleContextKeys::class, SampleContextKeys::FIRST.name).contextTags)
            .containsOnly(SECOND)
    }

    @Test
    fun `when a property to exclude does not exist then it is reported`() {
        assertThrows<IllegalArgumentException> {
            ReflectionContextTagsProvider(SampleContextKeys::class, "some-property")
        }
    }
}