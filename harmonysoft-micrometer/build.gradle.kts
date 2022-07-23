plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))

    api("io.micrometer:micrometer-core:${Version.MICROMETER}")

    testImplementation("org.mockito.kotlin:mockito-kotlin:${Version.MOCKITO_KOTLIN}")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:${Version.Kotlin.REFLECT}")
}