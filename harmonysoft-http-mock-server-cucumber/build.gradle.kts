plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-cucumber"))
    api(project(":harmonysoft-jackson"))
    api(project(":harmonysoft-http-mock-server-test"))

    testImplementation(project(":harmonysoft-http-client-apache-cucumber"))
    testImplementation(project(":internal-cucumber"))
}