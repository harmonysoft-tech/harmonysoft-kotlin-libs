package tech.harmonysoft.oss.common.string.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.harmonysoft.oss.common.string.util.ToStringUtil.HIDDEN_VALUE_PLACEHOLDER

internal class ToStringUtilTest {

    data class Credentials(val login: String, @HideValueInToString val password: String)
    data class PlainHolder(val data: String, val credentials: Credentials)
    data class PrivatePropertyHolder(val publicProp: String, private val privateProp: String)
    data class ArrayHolder(val array: Array<Int>)
    data class CollectionsHolder(
        val credentialsList: List<Credentials>,
        val credentialsSet: Set<Credentials>,
        val credentialsCollection: Collection<Credentials>,
        val credentialsKeys: Map<Credentials, Int>,
        val credentialsValues: Map<Int, Credentials>
    )

    val credentials = Credentials("my-login", "my-password")
    val credentialsString = ToStringUtil.build(credentials)

    @Test
    fun `when top-level property is marked to be hidden then it's respected`() {
        assertThat(credentialsString).isEqualTo("(login=my-login, password=$HIDDEN_VALUE_PLACEHOLDER)")
    }

    @Test
    fun `when nested property is marked to be hidden then it's respected`() {
        assertThat(ToStringUtil.build(PlainHolder("my-data", credentials))).isEqualTo(
            "(credentials=$credentialsString, data=my-data)"
        )
    }

    @Test
    fun `when collection nested property is marked to be hidden then it's respected`() {
        val holder = CollectionsHolder(
            credentialsList = listOf(credentials),
            credentialsSet = setOf(credentials),
            credentialsCollection = listOf(credentials),
            credentialsKeys = mapOf(credentials to 1),
            credentialsValues = mapOf(1 to credentials)
        )
        assertThat(ToStringUtil.build(holder)).isEqualTo(
            "(credentialsCollection=[$credentialsString], credentialsKeys={$credentialsString: 1}, "
            + "credentialsList=[$credentialsString], credentialsSet=[$credentialsString], "
            + "credentialsValues={1: $credentialsString})"
        )
    }

    @Test
    fun `when class has private properties then they are ignored`() {
        assertThat(ToStringUtil.build(PrivatePropertyHolder("test", "test-private"))).isEqualTo(
            "(publicProp=test)"
        )
    }

    @Test
    fun `when class has an array property then it's properly handled`() {
        assertThat(ToStringUtil.build(ArrayHolder(arrayOf(1, 2, 3)))).isEqualTo("(array=[1, 2, 3])")
    }
}