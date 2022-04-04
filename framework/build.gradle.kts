import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "2.6.6"
}

dependencies {
  implementation(kotlin("reflect"))

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("org.apache.activemq:activemq-spring:5.17.0")
  implementation("org.apache.commons:commons-io:1.3.2")
  implementation("org.apache.httpcomponents:httpclient:4.5")
  implementation("org.springframework.boot:spring-boot-configuration-processor:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-activemq:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-security:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-web:2.6.6")
  implementation("org.springframework:spring-context:5.3.18")

}

tasks.getByName<BootJar>("bootJar") {
  enabled = false
}

tasks.getByName<Jar>("jar") {
  enabled = true
  archiveClassifier.set("")
}