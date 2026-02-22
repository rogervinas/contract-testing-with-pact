import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  id("org.jetbrains.kotlin.jvm") version "2.3.10"
  id("au.com.dius.pact") version "4.7.0-beta.3"
  application
}

version = "1.0"

project.extra["pactbroker.url"] = project.properties["pactbroker.url"] ?: "http://localhost:9292"
project.extra["pacticipant"] = "Sample API Client"
project.extra["pacticipantVersion"] = version

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation(platform("io.ktor:ktor-bom:3.4.0"))
  implementation("io.ktor:ktor-client-core")
  implementation("io.ktor:ktor-client-cio")
  implementation("io.ktor:ktor-client-content-negotiation")
  implementation("io.ktor:ktor-serialization-jackson")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  testImplementation(platform("org.junit:junit-bom:6.0.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
  testImplementation("au.com.dius.pact.consumer:junit5:4.7.0-beta.3")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testLogging {
    events(PASSED, SKIPPED, FAILED)
    exceptionFormat = FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }

  systemProperties["pact.writer.overwrite"] = true
}

pact {
  publish {
    pactBrokerUrl = "${project.extra["pactbroker.url"]}"
  }
  broker {
    pactBrokerUrl = "${project.extra["pactbroker.url"]}"
  }
}

application {
  mainClass.set("com.rogervinas.sample.api.client.SampleAppKt")
}
