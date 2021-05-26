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
    maven { url = uri("https://packages.atlassian.com/mvn/maven-external/") }
    maven {
        val repositoryIssueTrackingJars: String by project.parent!!
        url = uri(repositoryIssueTrackingJars)
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation(project(":framework"))
    implementation(project(":jira-client"))
    implementation(project(":rtc-client"))

    implementation(kotlin("stdlib"))

    implementation("com.atlassian.jira:jira-rest-java-client-core:5.1.0-476bd700")
    implementation("com.ibm.team.rtc:plain-java-client:6.0.3")
    implementation("joda-time:joda-time:2.10.10")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(project(":test-utils"))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.mockito:mockito-core:3.10.0")
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