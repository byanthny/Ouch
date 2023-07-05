plugins {
  kotlin("multiplatform") version "1.8.21"
  kotlin("plugin.serialization") version "1.8.21"
  application
}

group = "com.springblossem"
version = "2.0.0"

object Versions {

  const val exposed = "0.40.1"
  const val kotlin = "1.8.20"
  const val coroutines = "1.7.1"
  const val serialization = "1.5.1"
  const val ktor = "2.3.1"
  const val logback = "1.4.7"
  const val compose = "1.4.2"
  const val fleks = "SNAPSHOT"
}

fun kotlinx(project: String, version: String) =
  """org.jetbrains.kotlinx:kotlinx-$project:$version"""

object ktor {

  operator fun invoke(module: String, version: String = Versions.ktor) =
    """io.ktor:ktor-$module:$version"""

  fun server(module: String, version: String = Versions.ktor) =
    invoke("server-$module", version)

  fun client(module: String, version: String = Versions.ktor) =
    invoke("client-$module", version)
}


repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
  jvm {
    jvmToolchain(17)
    withJava()
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }
  js(IR) {
    binaries.executable()
    browser {
      commonWebpackConfig {
        scssSupport {
          enabled.set(true)
        }
        cssSupport {
          enabled.set(true)
        }
      }
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val jvmMain by getting {
      dependencies {
        // Ktor
        implementation(kotlinx("html-jvm", "0.7.2"))
        implementation(ktor("serialization-kotlinx-json"))
        implementation(ktor.server("html-builder-jvm"))
        implementation(ktor.server("core"))
        implementation(ktor.server("cio"))
        implementation(ktor.server("cors"))
        implementation(ktor.server("content-negotiation"))
        implementation(ktor.server("websockets"))
        implementation(ktor.server("sessions"))
        implementation(ktor.server("auth"))
        implementation(ktor.server("call-logging"))

        // bcrypt
        implementation("at.favre.lib:bcrypt:0.10.2")

        // PostgreSQL
        implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed}")
        implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed}")
        implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}")
        implementation("org.postgresql:postgresql:42.6.0")

        // logging
        implementation("io.github.oshai:kotlin-logging-jvm:4.0.0")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(ktor.server("test-host"))
        implementation(ktor.client("content-negotiation"))
      }
    }
    val jsMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.346")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.2-pre.346")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.6-pre.346")
      }
    }
    val jsTest by getting
  }
}

application {
  mainClass.set("com.springblossem.ouch.server.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
  val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
  from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
  dependsOn(tasks.named<Jar>("jvmJar"))
  classpath(tasks.named<Jar>("jvmJar"))
}
