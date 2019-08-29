import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.21"
    id("org.springframework.boot") version "2.1.7.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "ch.loewenfels.issuetrackingsync"
version = "1.0-SNAPSHOT"
val springProfile = "test"
java.sourceCompatibility = JavaVersion.VERSION_1_8
// Atlassian JIRA and IBM RTC JARs are not available on maven central
// you'll need to set this property in your $HOME/.gradle/gradle.properties file to point to a maven repo holding these
// JARs. https://packages.atlassian.com/maven-public/ might work for JIRA
val repositoryIssueTrackingJars: String by project
repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri(repositoryIssueTrackingJars)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("org.apache.activemq:activemq-spring")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.ibm.team.rtc:plain-java-client:6.0.3")
    implementation("com.atlassian.jira:jira-rest-java-client-core:5.1.0-476bd700")
    implementation("io.atlassian.fugue:fugue:4.7.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    baseName = "app"
    classifier = ""
    version = ""
}
