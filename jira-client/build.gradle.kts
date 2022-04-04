import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "2.6.6"
}

repositories {
  maven {
    url = uri("https://packages.atlassian.com/mvn/maven-external")
  }
}

dependencies {
  implementation(project(":framework"))

  implementation("com.atlassian.jira:jira-rest-java-client-core:5.2.2")
  implementation("com.atlassian.renderer:atlassian-renderer:8.0.5") {
    exclude("javax.activation:activation:1.0.2")
  }
  implementation("io.atlassian.fugue:fugue:4.7.2")
  implementation("javax.activation:activation:1.1")
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("org.springframework.boot:spring-boot-configuration-processor:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-activemq:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-security:2.6.6")
  implementation("org.springframework.boot:spring-boot-starter-web:2.6.6")

  testImplementation(testFixtures(project(":test-utils")))

}


tasks.getByName<BootJar>("bootJar") {
  enabled = false
}

tasks.getByName<Jar>("jar") {
  enabled = true
  classifier = ""
}