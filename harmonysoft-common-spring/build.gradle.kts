plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))

    testImplementation(project(":harmonysoft-common-test"))
}