plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "tech.harmonysoft"
version = Version.APP

nexusPublishing {
    repositories {
        sonatype()
    }
}