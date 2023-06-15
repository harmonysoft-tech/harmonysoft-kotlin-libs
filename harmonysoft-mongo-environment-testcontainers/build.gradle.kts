plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-mongo-environment"))

    api("org.testcontainers:mongodb:${Version.TEST_CONTAINERS}")
}