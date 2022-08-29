plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-event-bus-api"))
    api(project(":harmonysoft-common-spring"))
}