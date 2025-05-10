plugins {
  kotlin("jvm") version "2.1.20"
  id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
  application
}

group = "com.lucasalfare"
version = "1.0"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation("com.github.LucasAlfare:FL-Base:1.1")
//  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
  implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.61.0")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

application {
  // Define the main class for the application.
  mainClass.set("com.lucasalfare.fldesk.MainKt")
}

tasks.withType<Jar> {
  manifest {
    // "Main-Class" is set to the actual main file path
    attributes["Main-Class"] = application.mainClass
  }

  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}