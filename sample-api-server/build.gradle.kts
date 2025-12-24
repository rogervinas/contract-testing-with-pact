import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  id("org.springframework.boot") version "4.0.1"
  id("io.spring.dependency-management") version "1.1.7"
  kotlin("jvm") version "2.3.0"
  kotlin("plugin.spring") version "2.3.0"
  id("au.com.dius.pact") version "4.7.0-beta.3"
}

version = "1.0"

project.extra["pactbroker.url"] = project.properties["pactbroker.url"] ?: "http://localhost:9292"
project.extra["pacticipant"] = "Sample API Server"
project.extra["pacticipantVersion"] = version

repositories {
  mavenCentral()
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

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("tools.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")

  testImplementation("au.com.dius.pact.provider:spring7:4.7.0-beta.3")

  testImplementation("com.ninja-squad:springmockk:5.0.1")
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

  systemProperties["pactbroker.url"] = "${project.extra["pactbroker.url"]}"
  systemProperties["pact.provider.version"] = version
  systemProperties["pact.verifier.publishResults"] = "true"
}

pact {
  broker {
    pactBrokerUrl = "${project.extra["pactbroker.url"]}"
  }
}
