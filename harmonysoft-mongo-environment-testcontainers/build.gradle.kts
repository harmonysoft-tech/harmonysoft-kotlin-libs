plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-mongo-test"))
    api(project(":harmonysoft-test-environment"))

    api("org.testcontainers:mongodb:${Version.TEST_CONTAINERS}")
}