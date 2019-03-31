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

    implementation("io.javalin:javalin:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.0-alpha")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.google.code.gson:gson:2.8.5")

    //
    implementation("org.slf4j:slf4j-simple:1.7.26")


    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common:1.3.21")
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

