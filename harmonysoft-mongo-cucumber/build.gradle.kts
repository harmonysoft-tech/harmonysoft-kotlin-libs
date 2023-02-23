plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-mongo-test"))
    api(project(":harmonysoft-common-cucumber"))

    testImplementation(project(":internal-cucumber"))
    testImplementation("de.bwaldvogel:mongo-java-server:1.43.0")
}