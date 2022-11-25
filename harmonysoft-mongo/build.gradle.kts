plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common"))

    // the same version is carried by spring-boot-mongo, implied by spring boot version configured in this project
    api("org.mongodb:mongodb-driver-sync:4.6.1")
}