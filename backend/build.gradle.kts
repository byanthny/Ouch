import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  application
  kotlin("jvm") version "1.8.22"
  id("kotlinx-serialization") version "1.8.22"
  //id("org.jmailen.kotlinter") version "2.1.2"
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.sim"
version = "0.0.3"

object Version {

  const val kmongo = "4.9.0"
  const val ktor = "2.3.1"
  const val klock = "4.0.2"
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  // Kotlin libraries
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20-RC")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20-RC")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

  // Time
  implementation("com.soywiz.korlibs.klock:klock-jvm:${Version.klock}")

  // Javalin Server
  implementation("io.javalin:javalin:5.6.0")

  // HTML
  implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")

  // Database
  implementation(
    "org.litote.kmongo", "kmongo-coroutine-serialization", Version.kmongo
  )
  // Cache
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.0")

  implementation("io.jsonwebtoken:jjwt-api:0.11.5")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

  implementation("commons-io", "commons-io", "2.6")

  // Misc
  implementation("org.slf4j:slf4j-simple:2.0.5")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "11"
}

tasks.withType<ShadowJar> {
  archiveVersion.set("")
}

application { mainClass.set("com.sim.ouch.LauncherKt") }

task("stage") {
  dependsOn("build", "clean")
}

