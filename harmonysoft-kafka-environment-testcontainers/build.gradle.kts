plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-kafka-environment"))

    api("org.testcontainers:kafka:${Version.TEST_CONTAINERS}")
}