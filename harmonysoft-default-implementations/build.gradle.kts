plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-event-bus-guava"))
    api(project(":harmonysoft-event-bus-spring"))
    api(project(":harmonysoft-jackson"))
    api("tech.harmonysoft:inpertio-client-kotlin-spring:${Version.INPERTIO}")
    api("tech.harmonysoft:inpertio-client-kotlin-event-harmonysoft:${Version.INPERTIO}")
}