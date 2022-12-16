plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-cucumber"))
    api(project(":harmonysoft-kafka-test"))
}