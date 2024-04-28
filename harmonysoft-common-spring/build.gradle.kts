plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))

    api("tech.harmonysoft:configurario-client-kotlin-spring:${Version.CONFIGURARIO}")

    testImplementation(project(":harmonysoft-common-test"))
}