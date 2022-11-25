package tech.harmonysoft.oss.common.auth.model

import tech.harmonysoft.oss.common.string.util.HideValueInToString
import tech.harmonysoft.oss.common.string.util.ToStringUtil

data class Credential(
    val login: String,
    @HideValueInToString val password: String
) {

    override fun toString(): String {
        return ToStringUtil.build(this)
    }
}