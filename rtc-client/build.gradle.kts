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
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven {
        val repositoryIssueTrackingJars: String by project.parent!!
        url = uri(repositoryIssueTrackingJars)
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation(project(":framework"))
    implementation(kotlin("stdlib"))
    implementation("com.ibm.team.rtc:plain-java-client:6.0.3")
    implementation("javax.activation:activation:1.1")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(testFixtures(project(":test-utils")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation("org.mockito:mockito-core:3.10.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
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