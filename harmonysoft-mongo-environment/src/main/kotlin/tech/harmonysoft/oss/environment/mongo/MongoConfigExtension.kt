package tech.harmonysoft.oss.environment.mongo

import tech.harmonysoft.oss.common.auth.model.Credential

interface MongoConfigExtension {

    object Default : MongoConfigExtension {

        override val db = "test"

        override val credential = Credential("test-user", "test-password")
    }

    val db: String
        get() = Default.db

    val credential: Credential
        get() = Default.credential
}