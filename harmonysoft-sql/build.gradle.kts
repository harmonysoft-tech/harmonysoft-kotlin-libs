plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))

    api("com.github.jsqlparser:jsqlparser:${Version.JSQL_PARSER}")

    testImplementation(project(":harmonysoft-common-test-spring"))
    testImplementation(project(":harmonysoft-event-bus-spring"))
    testImplementation(project(":harmonysoft-event-bus-guava"))
    testImplementation("tech.harmonysoft:inpertio-client-kotlin-spring:${Version.INPERTIO}")
    testImplementation("tech.harmonysoft:inpertio-client-kotlin-event-harmonysoft:${Version.INPERTIO}")
}