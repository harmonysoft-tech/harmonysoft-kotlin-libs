plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-mongo-test"))
    api(project(":harmonysoft-common-cucumber"))

    testImplementation(project(":internal-cucumber"))
    testImplementation(project(":harmonysoft-mongo-environment-testcontainers"))
}