package tech.harmonysoft.oss.kafka.service

import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Named
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.ConsumerGroupState
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.slf4j.Logger
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.json.JsonApi
import tech.harmonysoft.oss.kafka.config.TestKafkaConfig
import tech.harmonysoft.oss.kafka.config.TestKafkaConfigProvider
import tech.harmonysoft.oss.kafka.fixture.KafkaFixtureContext
import tech.harmonysoft.oss.kafka.fixture.KafkaTestFixture
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.fixture.FixtureDataHelper
import tech.harmonysoft.oss.test.json.CommonJsonUtil
import tech.harmonysoft.oss.test.util.VerificationUtil
import kotlin.concurrent.thread
import kotlin.reflect.KClass

@Named
class TestKafkaManager(
    private val configProvider: TestKafkaConfigProvider,
    private val fixtureHelper: FixtureDataHelper,
    private val jsonApi: JsonApi,
    private val dynamicContext: DynamicBindingContext,
    private val logger: Logger
) {

    private val topic2messages = ConcurrentHashMap<String, MutableList<ConsumerRecord<String, String>>>()
    private val topic2consumer = ConcurrentHashMap<String, KafkaConsumer<String, String>>()

    init {
        thread(isDaemon = true) {
            val pollDuration = Duration.ofMillis(300)
            while (true) {
                for ((topic, consumer) in topic2consumer) {
                    try {
                        val records = consumer.poll(pollDuration)
                        if (!records.isEmpty) {
                            for (record in records) {
                                logger.info(
                                    "received new kafka message from topic '{}': {}",
                                    topic, recordToString(record)
                                )
                                topic2messages.computeIfAbsent(topic) { CopyOnWriteArrayList() } += record
                            }
                        }
                    } catch (e: Exception) {
                        logger.warn("Unexpected exception occurred on attempt to get kafka messages "
                                    + "from topic '{}'", topic, e)
                    }
                }
            }
        }
    }

    private val headers = ConcurrentHashMap<String, String>()

    @AfterEach
    fun tearDown() {
        cleanAllHeaders()
        topic2messages.clear()
    }

    fun recordToString(record: ConsumerRecord<*, *>): String {
        val headers = record.headers().joinToString { "${it.key()}=${String(it.value())}" }
        return "headers=$headers, value=${record.value()}"
    }

    fun getAdmin(config: TestKafkaConfig): Admin {
        return Admin.create(mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to "${config.host}:${config.port}"
        ))
    }

    fun withProducer(action: (KafkaProducer<String, String>) -> Any) {
        withProducer(configProvider.data, action)
    }

    fun withProducer(config: TestKafkaConfig, action: (KafkaProducer<String, String>) -> Any) {
        val producer = KafkaProducer<String, String>(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "${config.host}:${config.port}",
                ProducerConfig.CLIENT_ID_CONFIG to "test",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
            )
        )
        producer.use {
            action(it)
        }
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
        logger.info("registered kafka message header {}={}", key, value)
    }

    fun cleanHeader(key: String) {
        headers.remove(key)
        logger.info("cleaned kafka message header '{}'", key)
    }

    fun cleanAllHeaders() {
        headers.clear()
        logger.info("cleaned all kafka message headers")
    }

    fun sendMessage(topic: String, message: String) {
        sendMessage(configProvider.data, topic, message)
    }

    fun sendMessage(config: TestKafkaConfig, topic: String, message: String) {
        withProducer(config) {
            val withExpandedMetaData = fixtureHelper.prepareTestData(
                type = KafkaTestFixture.TYPE,
                context = KafkaFixtureContext(topic),
                data = message
            ).toString()
            logger.info("sending kafka message to topic '{}'%nheaders: {}%ncontent: {}", topic, headers, message)
            it.send(ProducerRecord(
                topic,
                null, // partition
                null, // timestamp
                null, // key
                withExpandedMetaData, // value
                headers.map { e -> RecordHeader(e.key, e.value.toByteArray()) }
            ))
        }
    }

    fun createConsumer(topic: String): KafkaConsumer<String, String> {
        return createConsumer(
            topic = topic,
            config = configProvider.data,
            keyDeserializer = StringDeserializer::class,
            valueDeserializer = StringDeserializer::class
        )
    }

    fun <K : Any, V : Any> createConsumer(
        topic: String,
        config: TestKafkaConfig,
        keyDeserializer: KClass<out Deserializer<K>>,
        valueDeserializer: KClass<out Deserializer<V>>
    ): KafkaConsumer<K, V> {
        val consumerGroup = UUID.randomUUID().toString()
        val consumer = KafkaConsumer<K, V>(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "${config.host}:${config.port}",
                ConsumerConfig.GROUP_ID_CONFIG to consumerGroup,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to keyDeserializer.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to valueDeserializer.java
            )
        )
        consumer.subscribe(listOf(topic))

        // it takes some time for kafka to settle a consumer, that's why we wait here until that is done
        verifyConsumerGroupIsStableAfterConsumerAddition(config, consumerGroup, consumer)
        return consumer
    }

    fun ensureTopicExists(topic: String) {
        ensureTopicExists(configProvider.data, topic)
    }

    fun ensureTopicExists(config: TestKafkaConfig, topic: String) {
        logger.info("start checking if kafka topic '{}' exists", topic)
        val admin = getAdmin(config)
        try {
            val exists = admin.listTopics().listings().get(3000, TimeUnit.MILLISECONDS).any { it.name() == topic }
            if (exists) {
                logger.info("kafka topic '{}' already exists", topic)
                return
            }

            val result = admin.createTopics(listOf(NewTopic(topic, 1, 1)))
            result.values()[topic]?.get()
            logger.info("created kafka topic '{}'", topic)
        } finally {
            admin.close(Duration.ofMillis(500))
        }
    }

    fun verifyConsumerGroupIsStableAfterConsumerAddition(group: String, consumer: KafkaConsumer<*, *>) {
        verifyConsumerGroupIsStableAfterConsumerAddition(configProvider.data, group, consumer)
    }

    fun verifyConsumerGroupIsStableAfterConsumerAddition(
        config: TestKafkaConfig,
        group: String,
        consumer: KafkaConsumer<*, *>
    ) {
        val pollTtl = Duration.ofMillis(100)
        getAdmin(config).use { admin ->
            VerificationUtil.verifyConditionHappens(
                description = "kafka consumer group '$group' is ready after consumer addition",
                checkTtlSeconds = 60
            ) {
                // we need to poll periodically in order to make sure that our consumer is not automatically
                // dropped by kafka because of missing heartbeats
                consumer.poll(pollTtl)
                val consumerGroups = admin.listConsumerGroups().all().get()
                consumerGroups.find { it.groupId() == group }?.let { group ->
                    val optionalState = group.state()
                    if (optionalState.isPresent) {
                        val state = optionalState.get()
                        if (state == ConsumerGroupState.STABLE) {
                            ProcessingResult.success()
                        } else {
                            ProcessingResult.failure("kafka consumer group has state '$state'")
                        }
                    } else {
                        ProcessingResult.failure("no state is exposed for kafka consumer group '$group'")
                    }
                } ?: ProcessingResult.failure(
                    "kafka consumer group '$group' is not found, ${consumerGroups.size} group(s) are available: "
                    + consumerGroups.joinToString { it.groupId() }
                )
            }

        }
    }

    fun subscribe(topic: String) {
        ensureTopicExists(topic)
        if (topic2consumer.containsKey(topic)) {
            logger.info("kafka topic '{}' is already subscribed", topic)
            return
        }
        logger.info("subscribing kafka topic '{}'", topic)
        topic2consumer.computeIfAbsent(topic) { createConsumer(topic) }
        logger.info("subscribed kafka topic '{}'", topic)
    }

    fun verifyMessageIsReceived(expected: String, topic: String) {
        val preparedExpected = fixtureHelper.prepareTestData(
            type = KafkaTestFixture.TYPE,
            context = KafkaFixtureContext(topic),
            data = expected
        ).toString()
        verifyMessageIsReceived(expected, topic) {
            it.value() == preparedExpected
        }
    }

    fun verifyMessageWithTargetHeaderValueIsReceived(topic: String, headerKey: String, expectedHeaderValue: String) {
        verifyMessageIsReceived(
            expected = "a message with header $headerKey=$expectedHeaderValue",
            topic = topic
        ) { record ->
            record.headers()?.find { it.key() == headerKey && String(it.value()) == expectedHeaderValue } != null
        }
    }

    fun verifyMessageIsReceived(
        expected: String,
        topic: String,
        checker: (ConsumerRecord<String, String>) -> Boolean
    ) {
        val preparedExpected = fixtureHelper.prepareTestData(
            type = KafkaTestFixture.TYPE,
            context = KafkaFixtureContext(topic),
            data = expected
        ).toString()
        VerificationUtil.verifyConditionHappens("target message is received from kafka topic '$topic'") {
            val records = topic2messages[topic]
            if (records.isNullOrEmpty()) {
                ProcessingResult.failure("no messages are received from kafka topic '$topic'")
            } else {
                val found = records.any(checker)
                if (found) {
                    ProcessingResult.success()
                } else {
                    val error = buildString {
                        append("target message is not received from kafka topic '$topic'.\n")
                        append("expected:\n")
                        append(preparedExpected)
                        append("\n${records.size} message(s) are received:")
                        records.forEachIndexed { i, record ->
                            append("\n$i) ").append(recordToString(record))
                        }
                    }
                    ProcessingResult.failure(error)
                }
            }
        }
    }

    fun verifyJsonMessageIsReceived(expectedJson: String, topic: String) {
        val preparedExpected = fixtureHelper.prepareTestData(
            type = KafkaTestFixture.TYPE,
            context = KafkaFixtureContext(topic),
            data = CommonJsonUtil.prepareDynamicMarkers(expectedJson)
        ).toString()
        val parsedExpectedJson = jsonApi.parseJson(preparedExpected)
        verifyMessageIsReceived(preparedExpected, topic) {
            try {
                val actual = jsonApi.parseJson(it.value())
                val result = CommonJsonUtil.compareAndBind(
                    expected = parsedExpectedJson,
                    actual = actual,
                    strict = false
                )
                if (result.errors.isEmpty()) {
                    dynamicContext.storeBindings(result.boundDynamicValues)
                    true
                } else {
                    false
                }
            } catch (ignore: Exception) {
                false
            }
        }
    }

    fun verifyMessageIsNotReceived(expected: String, topic: String) {
        val preparedExpected = fixtureHelper.prepareTestData(
            type = KafkaTestFixture.TYPE,
            context = KafkaFixtureContext(topic),
            data = expected
        ).toString()
        VerificationUtil.verifyConditionDoesNotHappen {
            topic2messages[topic]?.find { record ->
                record.value() == preparedExpected
            }?.let {
                ProcessingResult.failure("received unexpected message in kafka topic '$topic': ${it.value()}")
            } ?: ProcessingResult.success()
        }
    }

    fun verifyJsonMessageIsNotReceived(expected: String, topic: String) {
        val preparedExpected = fixtureHelper.prepareTestData(
            type = KafkaTestFixture.TYPE,
            context = KafkaFixtureContext(topic),
            data = expected
        ).toString()
        val parsedExpectedJson = jsonApi.parseJson(preparedExpected)
        VerificationUtil.verifyConditionDoesNotHappen {
            topic2messages[topic]?.find { record ->
                val actual = jsonApi.parseJson(record.value())
                val result = CommonJsonUtil.compareAndBind(
                    expected = parsedExpectedJson,
                    actual = actual,
                    strict = false
                )
                result.errors.isEmpty()
            }?.let {
                ProcessingResult.failure("received unexpected message in kafka topic '$topic': ${it.value()}")
            } ?: ProcessingResult.success()
        }
    }
}