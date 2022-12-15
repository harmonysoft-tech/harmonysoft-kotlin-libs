plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("tech.harmonysoft.oss.gradle.release.paperwork") version "1.7.0"
}

group = "tech.harmonysoft"
version = Version.APP

releasePaperwork {
    projectVersionFile.set("buildSrc/src/main/kotlin/Version.kt")
    projectVersionRegex.set("""const val APP = "([^"]+)"""")
}

nexusPublishing {
    repositories {
        sonatype()
    }
}