plugins {
    id("harmonysoft-library-conventions")
}

dependencies {
    api(project(":harmonysoft-common-spring"))
    api(project(":harmonysoft-common-test"))

    api("org.junit.jupiter:junit-jupiter:${Version.JUNIT}")
    api("org.springframework.boot:spring-boot-starter-test")
}