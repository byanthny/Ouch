plugins {
  kotlin("multiplatform") version "1.8.21"
  kotlin("plugin.serialization") version "1.8.21"
  application
}

group = "com.springblossem"
version = "2.0.0"

object Versions {

  const val exposed = "0.40.1"
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
        implementation("io.ktor:ktor-server-netty:2.0.2")
        implementation("io.ktor:ktor-server-html-builder-jvm:2.0.2")
        implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

        // PostgreSQL
        implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed}")
        implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed}")
        implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}")
        implementation("org.postgresql:postgresql:42.6.0")
      }
    }
    val jvmTest by getting
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
