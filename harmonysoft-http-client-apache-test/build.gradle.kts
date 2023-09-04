plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-http-client-apache"))
    api(project(":harmonysoft-http-client-test"))
}