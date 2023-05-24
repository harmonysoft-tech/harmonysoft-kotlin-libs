package tech.harmonysoft.oss.cucumber.glue

import io.cucumber.datatable.DataTable
import io.cucumber.java.After
import io.cucumber.java.en.Given
import javax.inject.Inject
import tech.harmonysoft.oss.mongo.service.TestMongoManager

class MongoStepDefinitions {

    @Inject private lateinit var mongo: TestMongoManager

    @After
    fun cleanUpData() {
        mongo.cleanUpData()
    }

    @Given("^mongo ([^\\s]+) collection has the following documents?:$")
    fun ensureDocumentsExist(collection: String, data: DataTable) {
        mongo.ensureDocumentsExist(collection, data.asMaps())
    }

    @Given("^mongo ([^\\s]+) collection should have the following documents?:$")
    fun verifyDocumentsExist(collectionName: String, data: DataTable) {
        mongo.verifyDocumentsExist(collectionName, data.asMaps())
    }
}