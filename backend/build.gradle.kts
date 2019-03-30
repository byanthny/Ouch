import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    `java`
    `application`
}

group = "com.sim"
version = "0.0.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0-alpha")

    // Backend Web
    implementation("io.javalin:javalin:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.0-alpha")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")

    //
    implementation("org.slf4j:slf4j-simple:1.7.26")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application { mainClassName = "com.sim.ouch.LauncherKt" }
