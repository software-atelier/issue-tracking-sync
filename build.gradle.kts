import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.5.1"
}

group = "ch.loewenfels.issuetrackingsync"
version = "1.0-SNAPSHOT"
val springProfile = "test"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":framework"))
    implementation(project(":jira-client"))
    implementation(project(":rtc-client"))
    implementation(project(":jira-rtc-sync"))
}

detekt {
    failFast = true
    config = files("$projectDir/detekt-config.yml")

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins, Sonar etc.
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.withType<Detekt> {
    this.jvmTarget = "11"
}