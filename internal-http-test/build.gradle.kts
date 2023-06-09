plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":internal-cucumber"))
    api(project(":harmonysoft-http-mock-server-cucumber"))
    api(project(":harmonysoft-http-client-apache-cucumber"))
}

project.tasks.withType<PublishToMavenRepository>().configureEach { this.enabled = false }