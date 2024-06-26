rootProject.name = "harmonysoft-libs"

include("harmonysoft-common")
include("harmonysoft-common-spring")
include("harmonysoft-common-test")
include("harmonysoft-common-test-spring")
include("harmonysoft-common-cucumber")

include("harmonysoft-event-bus-api")
include("harmonysoft-event-bus-test")
include("harmonysoft-event-bus-guava")
include("harmonysoft-event-bus-spring")

include("harmonysoft-http-client")
include("harmonysoft-http-client-test")
include("harmonysoft-http-client-apache")
include("harmonysoft-http-client-apache-spring")
include("harmonysoft-http-client-apache-test")
include("harmonysoft-http-client-apache-cucumber")
include("harmonysoft-http-client-apache-cucumber-spring")

include("harmonysoft-http-mock-server-test")
include("harmonysoft-http-mock-server-cucumber")

include("harmonysoft-mongo")
include("harmonysoft-mongo-test")
include("harmonysoft-mongo-cucumber")
include("harmonysoft-mongo-environment")
include("harmonysoft-mongo-environment-testcontainers")
include("harmonysoft-mongo-environment-external")

include("harmonysoft-json-api")
include("harmonysoft-jackson")

include("harmonysoft-sql")

include("harmonysoft-micrometer")
include("harmonysoft-micrometer-influxdb")

include("harmonysoft-kafka-test")
include("harmonysoft-kafka-cucumber")
include("harmonysoft-kafka-environment")
include("harmonysoft-kafka-environment-testcontainers")
include("harmonysoft-kafka-environment-external")

include("harmonysoft-redis-test")
include("harmonysoft-redis-cucumber")
include("harmonysoft-redis-environment")
include("harmonysoft-redis-environment-testcontainers")
include("harmonysoft-redis-environment-external")

include("harmonysoft-slf4j-spring")

include("harmonysoft-default-implementations")

include("internal-cucumber")
include("internal-http-test")

include("harmonysoft-test-environment")
include("harmonysoft-test-environment-cucumber")