package tech.harmonysoft.oss.mongo.constant

import com.mongodb.BasicDBObject

object Mongo {

    object Filter {
        val ALL = BasicDBObject()
    }

    object Column {
        const val ID = "_id"
    }
}