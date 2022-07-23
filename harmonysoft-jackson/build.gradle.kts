plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))
    api(project(":harmonysoft-json-api"))

    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Version.JACKSON}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Version.JACKSON}")
}