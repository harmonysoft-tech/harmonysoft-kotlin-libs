package tech.harmonysoft.oss.mongo.service

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import javax.inject.Named
import org.slf4j.Logger
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.data.DataProviderStrategy
import tech.harmonysoft.oss.mongo.config.TestMongoConfigProvider
import tech.harmonysoft.oss.mongo.constant.Mongo
import tech.harmonysoft.oss.mongo.fixture.MongoTestFixture
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.input.CommonTestInputHelper
import tech.harmonysoft.oss.test.util.VerificationUtil

@Named
class TestMongoManager(
    private val configProvider: TestMongoConfigProvider,
    private val fixtureDataHelper: FixtureDataHelper,
    private val inputHelper: CommonTestInputHelper,
    private val bindingContext: DynamicBindingContext,
    private val logger: Logger
) {

    private val allDocumentsFilter = BasicDBObject()

    val client: MongoClient by lazy {
        val config = configProvider.data
        val auth = config.credential?.let {
            "${it.login}:${it.password}@"
        } ?: ""
        MongoClients.create("mongodb://$auth${config.host}:${config.port}/${config.db}")
    }

    fun cleanUpData() {
        val db = client.getDatabase(configProvider.data.db)
        for (collectionName in db.listCollectionNames()) {
            logger.info("Deleting all documents from mongo collection {}", collectionName)
            val result = db.getCollection(collectionName).deleteMany(allDocumentsFilter)
            logger.info("Deleted {} document(s) in mongo collection {}", result.deletedCount, collectionName)
        }
    }

    /**
     * Executes [ensureDocumentExists] for every given document.
     */
    fun ensureDocumentsExist(collection: String, documents: Collection<Map<String, String>>) {
        for (document in documents) {
            ensureDocumentExists(collection, document)
        }
    }

    /**
     * Checks if target collection contains a document with given data and inserts it in case of absence
     */
    fun ensureDocumentExists(collection: String, data: Map<String, String>) {
        val enrichedData = fixtureDataHelper.prepareTestData(MongoTestFixture.TYPE, Unit, data)
        val filter = BasicDBObject().apply {
            for ((key, value) in enrichedData) {
                this[key] = value
            }
        }
        client.getDatabase(configProvider.data.db).getCollection(collection).updateOne(
            filter,
            Updates.set("dummy", "dummy"),
            UpdateOptions().upsert(true)
        )
    }

    fun verifyDocumentsExist(collectionName: String, input: List<Map<String, String>>) {
        val records = inputHelper.parse(MongoTestFixture.TYPE, Unit, input)
        val projection = input.flatMap { it.keys }.toSet().toList()
        VerificationUtil.verifyConditionHappens {
            val collection = client.getDatabase(configProvider.data.db).getCollection(collectionName)
            val documents = collection
                .find(Mongo.Filter.ALL)
                .projection(Projections.include(projection))
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
}