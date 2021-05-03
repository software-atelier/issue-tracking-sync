import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
// Atlassian JIRA and IBM RTC JARs are not available on maven central
// you'll need to set this property in your $HOME/.gradle/gradle.properties file to point to a maven repo holding these
// JARs. https://packages.atlassian.com/maven-public/ might work for JIRA
val repositoryIssueTrackingJars: String by project
repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri(repositoryIssueTrackingJars)
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("org.apache.activemq:activemq-spring")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.ibm.team.rtc:plain-java-client:6.0.3")
    implementation("com.atlassian.jira:jira-rest-java-client-core:5.1.0-476bd700")
    implementation("io.atlassian.fugue:fugue:4.7.2")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.atlassian.renderer:atlassian-renderer:8.0.5") {
        exclude("javax.activation:activation:1.0.2")
    }
    implementation("javax.activation:activation:1.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    implementation("commons-httpclient:commons-httpclient:3.1")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
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

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    this.jvmTarget = "11"
}