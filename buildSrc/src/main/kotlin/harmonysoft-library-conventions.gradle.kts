plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.springframework.boot")
    `maven-publish`
    if (System.getenv("CI_ENV").isNullOrBlank()) {
        signing
    }
}

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

group = "tech.harmonysoft"
version = Version.APP

repositories {
    mavenCentral()
}

tasks.getByName("bootJar").enabled = false

dependencies {
    api("tech.harmonysoft:inpertio-client-jvm-api:${Version.INPERTIO}")
    api("javax.inject:javax.inject:${Version.JAVAX_INJECT}")
    api("javax.annotation:javax.annotation-api:${Version.JAVAX_ANNOTATION}")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${Version.Kotlin.COROUTINE}")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Version.Kotlin.COROUTINE}")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:${Version.Kotlin.REFLECT}")
    compileOnly("org.springframework.boot:spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core:${Version.ASSERTJ}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Version.JUNIT}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")
}

kotlin {
    jvmToolchain() {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.dokkaJavadoc.configure {
    outputDirectory.set(buildDir.resolve("dokkaJavadoc"))
    dokkaSourceSets {
        configureEach {
            noStdlibLink.set(false)
            noJdkLink.set(false)
        }
    }
}

val docJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(buildDir.resolve("dokkaJavadoc"))
}

val sourceJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    archives(docJar)
    archives(sourceJar)
}

publishing {
    publications {
        create<MavenPublication>("main") {
            artifactId = project.name
            from(components["java"])
            artifact(sourceJar)
            artifact(docJar)

            pom {
                name.set(project.name)
                description.set("Common general-purpose Kotlin utility")
                url.set("https://github.com/denis-zhdanov/harmonysoft-common")

                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("http://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("denis")
                        name.set("Denis Zhdanov")
                        email.set("denzhdanov@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://https://github.com/denis-zhdanov/harmonysoft-common")
                    developerConnection.set("scm:git:git://https://github.com/denis-zhdanov/harmonysoft-common")
                    url.set("https://github.com/denis-zhdanov/harmonysoft-common")
                }
            }
        }
    }
}

if (System.getenv("CI_ENV").isNullOrBlank()) {
    signing {
        sign(publishing.publications["main"])
    }
}