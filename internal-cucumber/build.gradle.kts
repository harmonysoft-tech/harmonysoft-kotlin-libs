plugins {
    id("harmonysoft-library-conventions")
}

project.tasks.publish.configure {
    enabled = false
}

dependencies {
    api(project(":harmonysoft-common-test-spring"))
    api(project(":harmonysoft-common-cucumber"))
    api(project(":harmonysoft-slf4j-spring"))
    api(project(":harmonysoft-default-implementations"))
}