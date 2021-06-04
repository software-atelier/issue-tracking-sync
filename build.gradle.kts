import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("org.springframework.boot") version "2.5.0"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "java-test-fixtures")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")


    group = "ch.loewenfels.issuetrackingsync"
    version = "2.0-SNAPSHOT"
    java.sourceCompatibility = JavaVersion.VERSION_11

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))

        testImplementation("org.hamcrest:hamcrest:2.2")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(module = "junit-vintage-engine")
        }
        testImplementation("org.mockito:mockito-core:3.10.0")
        testImplementation("org.springframework.security:spring-security-test")

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
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

