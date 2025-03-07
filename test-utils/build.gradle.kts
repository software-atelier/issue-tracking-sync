import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot")
}

dependencies {
  testFixturesImplementation(project(":framework"))

  testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testFixturesImplementation("org.springframework.boot:spring-boot-starter-activemq:2.6.6")
  testFixturesImplementation("org.springframework.boot:spring-boot-starter-security:2.6.6")
  testFixturesImplementation("org.springframework.boot:spring-boot-starter-test:2.6.6") {
    exclude(module = "junit-vintage-engine")
  }
}


tasks.getByName<BootJar>("bootJar") {
  enabled = false
}

tasks.getByName<Jar>("jar") {
  enabled = true
  classifier = ""
}