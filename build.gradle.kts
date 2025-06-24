object Props {
    const val VERSION = "0.3.11"
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

    // Add JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-framework-datatest:5.8.0") // For data-driven tests

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

/*tasks.dokkaJavadoc {
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(false)
            skipDeprecated.set(true)
            reportUndocumented.set(true)
            jdkVersion.set(8)
        }
    }
}*/

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

