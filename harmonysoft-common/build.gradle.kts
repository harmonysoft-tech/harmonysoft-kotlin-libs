plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    compileOnly("org.quartz-scheduler:quartz:${Version.QUARTZ}")

    testImplementation(project(":harmonysoft-common-test-spring"))
    testImplementation(project(":harmonysoft-event-bus-spring"))
    testImplementation(project(":harmonysoft-event-bus-guava"))

    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Version.JACKSON}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${Version.Kotlin.COROUTINE}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Version.Kotlin.COROUTINE}")
    testImplementation("org.quartz-scheduler:quartz:${Version.QUARTZ}")
    testImplementation("tech.harmonysoft:inpertio-client-kotlin-spring:${Version.INPERTIO}")
    testImplementation("tech.harmonysoft:inpertio-client-kotlin-event-harmonysoft:${Version.INPERTIO}")
}