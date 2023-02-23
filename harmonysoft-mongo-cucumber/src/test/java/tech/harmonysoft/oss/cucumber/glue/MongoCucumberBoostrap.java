package tech.harmonysoft.oss.cucumber.glue;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import tech.harmonysoft.oss.HarmonysoftTestApplication;
import tech.harmonysoft.oss.mongo.TestMongoConfigProviderImpl;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@CucumberContextConfiguration
@SpringBootTest(classes = HarmonysoftTestApplication.class)
public class MongoCucumberBoostrap {

    private static final AtomicReference<MongoServer> mongoHandle = new AtomicReference<>();
    private static final Logger logger = LoggerFactory.getLogger(MongoCucumberBoostrap.class);
    private static final AtomicInteger port = new AtomicInteger();

    private final AtomicBoolean initialized = new AtomicBoolean();

    @Inject private TestMongoConfigProviderImpl configProvider;

    @BeforeAll
    public static void startMongo() {
        logger.info("Starting embedded mongo");
        MongoServer server = new MongoServer(new MemoryBackend());
        InetSocketAddress address = server.bind();
        port.set(address.getPort());
        logger.info("Started embedded mongo on port {}", address.getPort());
        mongoHandle.set(server);
    }

    @AfterAll
    public static void stopMongo() {
        logger.info("Stopping embedded mongo");
        mongoHandle.get().shutdown();
        logger.info("Stopped embedded mongo");
    }

    @Before
    public void bootstrap() {
        if (initialized.compareAndSet(false, true)) {
            configProvider.setPort(port.get());
        }
    }
}
