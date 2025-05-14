val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val npmCommand = if (isWindows) "npm.cmd" else "npm"

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
  implementation("com.github.LucasAlfare:FL-Base:1.1.1")
  implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.61.0")
  testImplementation("io.ktor:ktor-server-test-host:3.1.1")
  testImplementation(kotlin("test"))
}

val buildWeb by tasks.registering(Exec::class) {
  workingDir = file("web-client")
  commandLine = listOf(npmCommand, "install")
}

val viteBuild by tasks.registering(Exec::class) {
  dependsOn(buildWeb)
  workingDir = file("web-client")
  commandLine = listOf(npmCommand, "run", "build")
}

val copyWebDist by tasks.registering(Copy::class) {
  dependsOn(viteBuild)
  from("web-client/dist")
  into("src/main/resources/assets")
  dependsOn(tasks.named("processResources"))
}

tasks.register("fullBuild") {
  group = "build"
  description = "Builds frontend and assembles backend"
  dependsOn(copyWebDist)
  dependsOn("assemble") // build backend without tests
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