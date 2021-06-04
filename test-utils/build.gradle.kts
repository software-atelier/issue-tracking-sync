import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("java-library")
    id("java-test-fixtures")
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "ch.loewenfels.issuetrackingsync"
version = "1.0-SNAPSHOT"
val springProfile = "test"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    testFixturesImplementation(project(":framework"))
    testFixturesImplementation(kotlin("stdlib"))

    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-activemq")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-security")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit-vintage-engine")
    }

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}