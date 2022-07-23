plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-http-client-apache-cucumber"))
    api(project(":harmonysoft-common-spring"))
}