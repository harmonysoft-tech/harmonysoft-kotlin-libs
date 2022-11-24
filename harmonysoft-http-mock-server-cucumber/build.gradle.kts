plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-cucumber"))
    api(project(":harmonysoft-jackson"))
    api("org.mock-server:mockserver-netty:5.14.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }
}