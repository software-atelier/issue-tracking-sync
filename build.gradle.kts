import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  id("io.gitlab.arturbosch.detekt") version "1.5.1"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("org.jetbrains.kotlin.jvm") version "1.5.10"
  id("org.springframework.boot") version "2.6.6"
  id("maven-publish")
}

allprojects {
  apply(plugin = "java")
  apply(plugin = "java-library")
  apply(plugin = "java-test-fixtures")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "io.spring.dependency-management")
  apply(plugin = "maven-publish")


  group = "ch.loewenfels.issuetrackingsync"

  java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
  }

  repositories {
    mavenCentral()
    maven { url = uri("https://packages.atlassian.com/mvn/maven-external/") }
  }

  dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.6") {
      exclude(module = "junit-vintage-engine")
    }
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.springframework.security:spring-security-test:5.6.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "11"
    }
  }

  publishing {
    repositories {
      maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/loewenfels/issue-tracking-sync")
        credentials {
          username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
          password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
      }
    }
    publications {
      register<MavenPublication>("gpr") {
        from(components["java"])
      }
    }
  }
}

dependencies {
  implementation(project(":framework"))
  implementation(project(":jira-client"))
}

detekt {
  failFast = true
  config = files("$projectDir/detekt-config.yml")

  reports {
    html.enabled = true // observe findings in your browser with structure and code snippets
    xml.enabled = true // checkstyle like format mainly for integrations like Jenkins, Sonar etc.
  }
}

tasks.withType<BootRun> {
  main = ("ch.loewenfels.issuetrackingsync.app.IssueTrackingSyncApp")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
  mainClass.set("ch.loewenfels.issuetrackingsync.app.IssueTrackingSyncApp")
}

