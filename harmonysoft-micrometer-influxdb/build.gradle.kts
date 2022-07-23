plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))
    api(project(":harmonysoft-http-client-apache"))

    api("io.micrometer:micrometer-core:${Version.MICROMETER}")
}