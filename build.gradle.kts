import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.dokka") version "1.9.0"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

group = "com.github.mdjkenna"

tasks.test {
    useJUnit()
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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0")
    testImplementation(kotlin("test"))
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("junit:junit:4.13.2")
}

// Task to generate sources jar
tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

// Task to generate Dokka Javadoc jar
tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.named("dokkaJavadoc"))
    from(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
}

tasks.dokkaJavadoc.configure {
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(true) // Exclude non-public members
            reportUndocumented.set(true) // Warn about undocumented members
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.github.mdjkenna"
            artifactId = "GraphStateMachine"
            version = "0.3.4"

            // Include the sources jar
            artifact(tasks.named("sourcesJar"))

            // Include the javadoc jar
            artifact(tasks.named("dokkaJavadocJar"))
        }
    }
}

