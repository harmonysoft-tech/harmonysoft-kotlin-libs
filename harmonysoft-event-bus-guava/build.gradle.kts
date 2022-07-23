plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-event-bus-api"))

    api("com.google.guava:guava:${Version.GUAVA}")
}