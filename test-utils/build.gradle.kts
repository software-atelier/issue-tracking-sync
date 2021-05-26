import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "ch.loewenfels.issuetrackingsync"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":framework"))
    implementation(kotlin("stdlib"))

    implementation("org.hamcrest:hamcrest:2.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    implementation("org.mockito:mockito-core:3.10.0")
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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