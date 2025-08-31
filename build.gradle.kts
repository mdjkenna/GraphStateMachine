import java.io.FileInputStream
import java.util.*

data class GsmUserAndPassword(val gsmUser : String, val gsmPassword : String)

val gsmUserAndPassword : GsmUserAndPassword by lazy {
    var gsmUser : String? = System.getenv("GSM_USER")
    var gsmPassword: String? = System.getenv("GSM_PASSWORD")

    if(gsmUser.isNullOrBlank() || gsmPassword.isNullOrBlank()) {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            val properties = Properties()
            FileInputStream(localPropertiesFile).use { properties.load(it) }
            properties.forEach { (key, value) ->
                extra[key.toString()] = value
            }

            gsmUser = properties["GSM_USER"] as? String ?: throw Exception("GSM User not found")
            gsmPassword = properties["GSM_PASSWORD"] as? String ?: throw Exception("GSM Password not found")
        }
    }

    GsmUserAndPassword(
        gsmUser ?: throw Exception("GSM User not found"),
        gsmPassword ?: throw Exception("GSM Password not found")
    )
}

object Props {
    const val VERSION = "1.00-beta01"
}

plugins {
    kotlin("jvm") version "2.1.0"
    id("jacoco")
    `maven-publish`
    id("org.jetbrains.dokka") version "1.9.0"
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

repositories {
    mavenCentral()
}

group = "com.github.mdjkenna"
version = Props.VERSION

tasks.test {
   useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-framework-datatest:5.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(false)
            skipDeprecated.set(true)
            reportUndocumented.set(true)
            jdkVersion.set(8)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "GraphStateMachine"
            version = Props.VERSION

            from(components["java"])

            pom {
                name.set("GraphStateMachine")
                description.set("A Kotlin library for creating graph based state machines")
                url.set("https://github.com/mdjkenna/GraphStateMachine")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("mdjkenna")
                        name.set("mdjkenna")
                        email.set("mdjkenna5813@gmail.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/mdjkenna/GraphStateMachine.git")
                    developerConnection.set("scm:git:ssh://github.com:mdjkenna/GraphStateMachine.git")
                    url.set("https://github.com/mdjkenna/GraphStateMachine")
                }
            }
        }
    }
}

