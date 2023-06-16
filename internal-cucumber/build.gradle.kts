plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-test-spring"))
    api(project(":harmonysoft-common-cucumber"))
    api(project(":harmonysoft-test-environment"))
    api(project(":harmonysoft-slf4j-spring"))
    api(project(":harmonysoft-default-implementations"))
}