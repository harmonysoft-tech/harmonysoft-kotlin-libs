package tech.harmonysoft.oss.cucumber.glue

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import io.cucumber.datatable.DataTable
import io.cucumber.java.After
import io.cucumber.java.en.Given
import javax.inject.Inject
import org.slf4j.Logger
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.data.DataProviderStrategy
import tech.harmonysoft.oss.cucumber.input.CommonCucumberInputHelper
import tech.harmonysoft.oss.mongo.config.TestMongoConfigProvider
import tech.harmonysoft.oss.mongo.constant.Mongo
import tech.harmonysoft.oss.mongo.fixture.MongoTestFixture
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.util.VerificationUtil

class MongoStepDefinitions {

    private val allDocumentsFilter = BasicDBObject()

    @Inject private lateinit var configProvider: TestMongoConfigProvider
    @Inject private lateinit var bindingContext: DynamicBindingContext
    @Inject private lateinit var cucumberInputHelper: CommonCucumberInputHelper
    @Inject private lateinit var fixtureDataHelper: FixtureDataHelper
    @Inject private lateinit var logger: Logger

    val client: MongoClient by lazy {
        val config = configProvider.data
        val auth = config.credential?.let {
            "${it.login}:${it.password}@"
        } ?: ""
        MongoClients.create("mongodb://$auth${config.host}:${config.port}/${config.db}")
    }

    @After
    fun cleanUpData() {
        val db = client.getDatabase(configProvider.data.db)
        for (collectionName in db.listCollectionNames()) {
            logger.info("Deleting all documents from mongo collection {}", collectionName)
            val result = db.getCollection(collectionName).deleteMany(allDocumentsFilter)
            logger.info("Deleted {} document(s) in mongo collection {}", result.deletedCount, collectionName)
        }
    }

    @Given("^mongo ([^\\s]+) collection has the following documents?:$")
    fun ensureDocumentExists(collection: String, data: DataTable) {
        val enrichedData = data.asMaps().map { map ->
            fixtureDataHelper.prepareTestData(MongoTestFixture.TYPE, Unit, map)
        }
        for (documentData in enrichedData) {
            ensureDocumentExists(collection, documentData)
        }
    }

    fun ensureDocumentExists(collection: String, data: Map<String, Any?>) {
        val filter = BasicDBObject().apply {
            for ((key, value) in data) {
                this[key] = value
            }
        }
        client.getDatabase(configProvider.data.db).getCollection(collection).updateOne(
            filter,
            Updates.set("dummy", "dummy"),
            UpdateOptions().upsert(true)
        )
    }

    @Given("^mongo ([^\\s]+) collection should have the following documents?:$")
    fun verifyDocumentsExist(collectionName: String, data: DataTable) {
        val records = cucumberInputHelper.parse(MongoTestFixture.TYPE, Unit, data)
        VerificationUtil.verifyConditionHappens {
            val collection = client.getDatabase(configProvider.data.db).getCollection(collectionName)
            val documents = collection
                .find(Mongo.Filter.ALL)
                .projection(Projections.include((records.first().data.keys + records.first().toBind.keys).toList()))
                .toList()
                .map { it.toMap() }

            for (record in records) {
                val result = VerificationUtil.find(
                    expected = record.data,
                    candidates = documents,
                    keys = record.data.keys,
                    retrievalStrategy = DataProviderStrategy.fromMap(),
                )
                if (!result.success) {
                    return@verifyConditionHappens result.mapError()
                }
                val matched = result.successValue
                for ((column, key) in record.toBind) {
                    bindingContext.storeBinding(key, matched[column])
                }
            }
            ProcessingResult.success()
        }
    }

    fun verifyDocumentExists(
        collectionName: String,
        data: Map<String, Any>,
        additionalProjection: Set<String>
    ): Map<String, Any?> {
        val collection = client.getDatabase(configProvider.data.db).getCollection(collectionName)
        val documents = collection
            .find(Mongo.Filter.ALL)
            .projection(Projections.include((data.keys + additionalProjection).toList()))
            .toList()
            .map { it.toMap() }
        return VerificationUtil.verifyContains(data, documents, data.keys, DataProviderStrategy.fromMap())
    }
}