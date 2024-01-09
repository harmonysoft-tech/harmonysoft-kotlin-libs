package tech.harmonysoft.oss.mongo.service

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Named
import org.bson.BSONObject
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.fail
import org.slf4j.Logger
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.collection.CollectionUtil
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.mongo.config.TestMongoConfig
import tech.harmonysoft.oss.mongo.config.TestMongoConfigProvider
import tech.harmonysoft.oss.mongo.constant.Mongo
import tech.harmonysoft.oss.mongo.fixture.MongoTestFixture
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.input.CommonTestInputHelper
import tech.harmonysoft.oss.test.input.TestInputRecord
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.manager.CommonTestManager
import tech.harmonysoft.oss.test.util.VerificationUtil

@Named
class TestMongoManager(
    private val configProvider: TestMongoConfigProvider,
    private val inputHelper: CommonTestInputHelper,
    private val fixtureHelper: FixtureDataHelper,
    private val bindingContext: DynamicBindingContext,
    private val jsonApi: JsonApi,
    private val common: CommonTestManager,
    private val logger: Logger
) {

    private val allDocumentsFilter = BasicDBObject()

    val client: MongoClient by lazy {
        getClient(configProvider.data)
    }

    @AfterEach
    fun cleanUpData() {
        val db = client.getDatabase(configProvider.data.db)
        for (collectionName in db.listCollectionNames()) {
            logger.info("Deleting all documents from mongo collection {}", collectionName)
            val result = db.getCollection(collectionName).deleteMany(allDocumentsFilter)
            logger.info("Deleted {} document(s) in mongo collection {}", result.deletedCount, collectionName)
        }
    }

    fun getClient(config: TestMongoConfig): MongoClient {
        val auth = config.credential?.let {
            "${it.login}:${it.password}@"
        } ?: ""
        val connectionString = "mongodb://$auth${config.host}:${config.port}/${config.db}"
        val timeoutMs = 100
        val settings = MongoClientSettings
            .builder()
            .applyToSocketSettings {
                it.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                it.readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            }
            .applyToClusterSettings {
                it.serverSelectionTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
            }
            .applyConnectionString(ConnectionString(connectionString))
            .build()
        return MongoClients.create(settings)
    }

    /**
     * Executes [ensureDocumentExists] for every given document.
     */
    fun ensureDocumentsExist(collection: String, documents: Collection<Map<String, String>>) {
        for (document in documents) {
            ensureDocumentExists(collection, document)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun ensureJsonDocumentExist(collection: String, json: String) {
        // there is a possible case that we define a document with 'null' values of some properties, like below:
        // { "key": <null> }
        // If we try to expand meta values in such string, that would fail because <null> meta value should be
        // expanded as 'null' but it's a part of the string, so, such an expansion attempt would result in failure.
        // That's why here we explicitly replace null meta value (<null>) by UUID and then replace it back to null
        // in the parsed values
        val nullReplacement = UUID.randomUUID().toString()
        val jsonWithNullsReplaced = json.replace("<null>", "\"$nullReplacement\"")
        val preparedJson = fixtureHelper.prepareTestData(
            type = MongoTestFixture.TYPE,
            context = Any(),
            data = CommonJsonUtil.prepareDynamicMarkers(jsonWithNullsReplaced)
        ).toString()
        val parsed = jsonApi.parseJson(preparedJson)
        val documentData = CollectionUtil.flatten(parsed as Map<String, Any?>).mapValues { (_, value) ->
            if (value == nullReplacement) {
                "<null>"
            } else {
                value
            }
        }
        ensureDocumentExists(collection, toStringValues(documentData))
    }

    private fun toStringValues(data: Map<String, Any?>): Map<String, String> {
        return data.mapValues { (key, value) ->
            when (value) {
                is String -> value
                is Int -> "<int($value)>"
                is Float -> "<float($value)>"
                is Double -> "<double($value)>"
                null -> "<null>"
                else -> fail(
                    "can not convert mongo input data's value for key '$key' - it has unsupported type "
                    + "${value::class.qualifiedName} ($value)"
                )
            }
        }
    }

    /**
     * Checks if target collection contains a document with given data and inserts it in case of absence
     */
    @Suppress("UNCHECKED_CAST")
    fun ensureDocumentExists(collection: String, data: Map<String, String>) {
        val record = inputHelper.parse(MongoTestFixture.TYPE, Unit, data).let {
            it.copy(data = CollectionUtil.unflatten(it.data))
        }
        val filter = toBson(CommonJsonUtil.dropDynamicMarkers(record.data) as Map<String, Any>)
        client.getDatabase(configProvider.data.db).getCollection(collection).updateOne(
            filter,
            Updates.set("dummy", "dummy"),
            UpdateOptions().upsert(true)
        )
        verifyDocumentsExist(collection, listOf(data))
    }

    fun toBson(data: Map<String, Any?>): Bson {
        val result = BasicDBObject()
        for ((key, value) in data) {
            if (value is Map<*, *> || value is Collection<*>) {
                result[key] = toBsonObject(value)
            } else {
                result[key] = value
            }
        }
        return result
    }

    private fun toBsonObject(data: Any): BSONObject {
        return when (data) {
            is Map<*, *> -> BasicDBObject().apply {
                for ((key, value) in data) {
                    if (key != null && value != null) {
                        if (value is Map<*, *> || value is Collection<*>) {
                            this[key.toString()] = toBsonObject(value)
                        } else {
                            this[key.toString()] = value
                        }
                    }
                }
            }

            is Collection<*> -> BasicDBList().apply {
                for (value in data) {
                    if (value != null) {
                        if (value is Map<*, *> || value is Collection<*>) {
                            add(toBsonObject(value))
                        } else {
                            add(value)
                        }
                    }
                }
            }

            else -> throw IllegalArgumentException(
                "only collections and maps are supported, but got ${data::class.qualifiedName}: $data"
            )
        }
    }

    fun toCollections(dbData: Any): Any {
        return when (dbData) {
            is Document -> dbData.entries.map { (key, value) ->
                key to value?.let {
                    toCollections(value)
                }
            }.toMap()

            is Collection<*> -> dbData.mapNotNull { value ->
                value?.let {
                    toCollections(it)
                }
            }

            else -> dbData
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun verifyJsonDocumentExists(collectionName: String, json: String) {
        val preparedJson = fixtureHelper.prepareTestData(
            type = MongoTestFixture.TYPE,
            context = Any(),
            data = CommonJsonUtil.prepareDynamicMarkers(json)
        ).toString()
        val parsed = jsonApi.parseJson(preparedJson)
        val data = toStringValues(CollectionUtil.flatten(parsed as Map<String, Any?>))
        verifyDocumentsExist(collectionName, listOf(data))
    }

    fun verifyDocumentsExist(collectionName: String, input: List<Map<String, String>>) {
        val records = inputHelper.parse(MongoTestFixture.TYPE, Unit, input).map {
            it.copy(data = CollectionUtil.unflatten(it.data))
        }
        val projection = (records.flatMap {
            it.data.keys + it.toBind.keys
        }).toSet().toList()

        val actualDocumentsFetcher = {
            val collection = client.getDatabase(configProvider.data.db).getCollection(collectionName)
            collection
                .find(Mongo.Filter.ALL)
                .projection(Projections.include(projection))
                .map { document ->
                    document.apply {
                        document[Mongo.Column.ID]?.let { id ->
                            if (id is ObjectId) {
                                document[Mongo.Column.ID] = id.toString()
                            }
                        }
                    }
                }.map { toCollections(it) }.toList()
        }

        if (common.expectTestVerificationFailure) {
            VerificationUtil.verifyConditionHappens(
                "target document is not found in mongo '$collectionName' collection"
            ) {
                val actual = actualDocumentsFetcher()
                matchDocuments(records, actual)?.let {
                    ProcessingResult.success()
                } ?: ProcessingResult.failure("target document is found in mongo '$collectionName' collection")
            }
        } else {
            VerificationUtil.verifyConditionHappens(
                "target document is found in mongo '$collectionName' collection"
            ) {
                val actual = actualDocumentsFetcher()
                matchDocuments(records, actual)?.let {
                    ProcessingResult.failure(it)
                } ?: ProcessingResult.success()
            }
        }
    }

    private fun matchDocuments(expected: Collection<TestInputRecord>, actual: Collection<Any>): String? {
        if (actual.isEmpty()) {
            return "no actual documents are available"
        }
        val candidates = actual.toMutableList()
        val allErrors = mutableListOf<String>()
        for (record in expected) {
            val candidateMismatches = mutableListOf<String>()
            for (candidate in candidates) {
                val error = matchDocument(record, candidate)
                if (error == null) {
                    candidates.remove(candidate)
                    candidateMismatches.clear()
                    break
                } else {
                    candidateMismatches += error
                }
            }
            if (candidateMismatches.isNotEmpty()) {
                allErrors += buildString {
                    append("can not find mongo document with the following data - '$record':")
                    for (mismatchDescription in candidateMismatches) {
                        append("  ")
                        append(mismatchDescription)
                    }
                }
            }
        }
        return if (allErrors.isEmpty()) {
            null
        } else {
            buildString {
                append("found ${allErrors.size} error(s):")
                allErrors.forEachIndexed { index, error ->
                    if (index > 0) {
                        append("\n--------------------------------------------------")
                    }
                    append("\n")
                    append(error)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun matchDocument(expected: TestInputRecord, actual: Any): String? {
        val result = CommonJsonUtil.compareAndBind(
            expected = expected.data,
            actual = actual,
            strict = false,
            equalityMatcher = this::matcher
        )
        val resolvedBindings = mutableMapOf<DynamicBindingKey, Any?>()
        val errors = mutableListOf<String>()
        if (expected.toBind.isNotEmpty()) {
            val flattened = CollectionUtil.flatten(actual as Map<String, Any?>)
            for ((propertyPath, dynamicKey) in expected.toBind) {
                val value = flattened[propertyPath]
                if (value == null) {
                    errors += "no value to bind is found at path '$propertyPath'"
                } else {
                    resolvedBindings[dynamicKey] = value
                }
            }
        }
        errors += result.errors
        return if (errors.isEmpty()) {
            bindingContext.storeBindings(result.boundDynamicValues)
            bindingContext.storeBindings(resolvedBindings)
            null
        } else {
            buildString {
                append("there are ${errors.size} mismatch(es) with document '$actual':")
                for (error in errors) {
                    append("\n    *) ")
                    append(error)
                }
            }
        }
    }

    private fun matcher(o1: Any, o2: Any): Boolean {
        return normalise(o1) == normalise(o2)
    }

    private fun normalise(data: Any): Any {
        return when (data) {
            is Decimal128 -> data.toDouble()
            else -> data
        }
    }
}