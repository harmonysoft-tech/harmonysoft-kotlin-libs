plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-kafka-test"))
    api(project(":harmonysoft-common-cucumber"))

    testImplementation(project(":harmonysoft-test-environment-cucumber"))
    testImplementation(project(":harmonysoft-kafka-environment-testcontainers"))
    testImplementation(project(":internal-cucumber"))
}