package tech.harmonysoft.oss.test.binding

data class DynamicBindingKey(val id: String) {

    init {
        if (id.isBlank()) {
            throw IllegalArgumentException("binding id must not be blank")
        }
    }

    override fun toString(): String {
        return id
    }
}