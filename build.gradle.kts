import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Props {
    const val VERSION = "0.3.8"
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
    useJUnit()
}

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("junit:junit:4.13.2")
}

tasks.dokkaJavadoc {
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
            }

        }
    }
}

