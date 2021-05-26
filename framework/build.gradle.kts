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
val springProfile = "test"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}


dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("org.apache.httpcomponents:httpclient:4.5")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
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