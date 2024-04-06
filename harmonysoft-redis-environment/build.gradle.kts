plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-redis-test"))
    api(project(":harmonysoft-test-environment"))
}