plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))

    api("org.junit.jupiter:junit-jupiter:${Version.JUNIT}")
    api("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")
}