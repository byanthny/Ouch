import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.21"
    id("kotlinx-serialization") version "1.3.21"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "com.sim"
version = "0.0.0"

val ktor_version = "1.1.3"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0-alpha")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.0-alpha")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common:1.3.21")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")

    implementation("io.javalin:javalin:2.8.0")
    implementation("org.thymeleaf:thymeleaf:3.0.9.RELEASE")
    implementation("com.atlassian.commonmark:commonmark:0.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.google.code.gson:gson:2.8.5")
    api("io.jsonwebtoken:jjwt-api:0.10.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.10.5")
    runtimeOnly("io.jsonwebtoken:jjwt-orgjson:0.10.5") {
        exclude(group = "org.json", module = "json" )
    }

    implementation("org.litote.kmongo:kmongo-async:3.10.0")
    implementation("org.litote.kmongo:kmongo-coroutine:3.10.0")

    implementation("org.slf4j:slf4j-simple:1.7.26")
    testImplementation("junit:junit:4.12")
    testImplementation("io.ktor:ktor-client-websocket:$ktor_version")
    testImplementation("io.ktor:ktor-client-cio:$ktor_version")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application { mainClassName = "com.sim.ouch.LauncherKt" }

task("stage") {
    dependsOn("build", "clean")
}

