import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.50"
    id("kotlinx-serialization") version "1.3.50"
    id("org.jmailen.kotlinter") version "2.1.2"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "com.sim"
version = "0.0.3"

object Version {
    const val kmongo = "3.11.1"
    const val ktor = "1.1.3"
    const val klock = "1.7.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Kotlin libraries
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0-alpha")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0")

    // Time
    implementation("com.soywiz.korlibs.klock:klock-jvm:${Version.klock}")

    // Javalin Server
    implementation("io.javalin:javalin:3.5.0")

    // HTML
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")
    implementation("org.thymeleaf:thymeleaf:3.0.9.RELEASE")
    implementation("com.atlassian.commonmark:commonmark:0.11.0")

    // Database
    implementation(
        "org.litote.kmongo", "kmongo-coroutine-serialization", Version.kmongo
    )

    // Cache
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.0")

    implementation("io.jsonwebtoken:jjwt-api:0.10.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.10.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.10.5")

    implementation("commons-io", "commons-io", "2.6")

    // Misc
    implementation("org.slf4j:slf4j-simple:1.7.26")

    // Testing
    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common:1.3.21")
    testImplementation("io.ktor:ktor-client-websocket:${Version.ktor}")
    testImplementation("io.ktor:ktor-client-cio:${Version.ktor}")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application { mainClassName = "com.sim.ouch.LauncherKt" }

task("stage") {
    dependsOn("build", "clean")
}

