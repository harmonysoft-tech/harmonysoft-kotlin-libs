plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-redis-test"))
    api(project(":harmonysoft-common-cucumber"))

    testImplementation(project(":internal-cucumber"))
    testImplementation(project(":harmonysoft-redis-environment-testcontainers"))
}