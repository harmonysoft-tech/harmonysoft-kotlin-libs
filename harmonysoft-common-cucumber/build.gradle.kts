plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-test"))

    api("io.cucumber:cucumber-java:${Version.CUCUMBER}")
    api("io.cucumber:cucumber-junit:${Version.CUCUMBER}")
    api("io.cucumber:cucumber-spring:${Version.CUCUMBER}")
}