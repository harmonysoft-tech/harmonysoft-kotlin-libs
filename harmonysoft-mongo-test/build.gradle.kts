plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-json-api"))
    api(project(":harmonysoft-mongo"))
    api(project(":harmonysoft-common-test"))
}