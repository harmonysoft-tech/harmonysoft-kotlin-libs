plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-http-client-apache-test"))
    api(project(":harmonysoft-common-cucumber"))
    api(project(":harmonysoft-json-api"))

    implementation("org.assertj:assertj-core:${Version.ASSERTJ}")
}