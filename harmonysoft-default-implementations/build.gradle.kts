plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-event-bus-guava"))
    api(project(":harmonysoft-event-bus-spring"))
    api(project(":harmonysoft-jackson"))
    api("tech.harmonysoft:configurario-client-kotlin-spring:${Version.CONFIGURARIO}")
}