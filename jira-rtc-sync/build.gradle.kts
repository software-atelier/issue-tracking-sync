repositories {
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


    implementation("com.atlassian.jira:jira-rest-java-client-core:5.2.2")
    implementation("com.ibm.team.rtc:plain-java-client:6.0.3")
    implementation("joda-time:joda-time:2.10.10")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(testFixtures(project(":test-utils")))
}