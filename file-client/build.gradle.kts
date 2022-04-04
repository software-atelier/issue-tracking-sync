import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot")
}

dependencies {
  implementation(project(":framework"))

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("org.springframework.boot:spring-boot-configuration-processor:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-activemq:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-security:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-web:2.6.6")
}

tasks.getByName<BootJar>("bootJar") {
  enabled = false
}

tasks.getByName<Jar>("jar") {
  enabled = true
  classifier = ""
}