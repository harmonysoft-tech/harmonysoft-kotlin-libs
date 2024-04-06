plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-redis-environment"))

    api("org.testcontainers:testcontainers:${Version.TEST_CONTAINERS}")
}