plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-test"))
    api(project(":harmonysoft-json-api"))

    // the same version is carried by spring-kafka, implied by spring boot version configured in this project
    api("org.apache.kafka:kafka-clients:3.1.1")
}