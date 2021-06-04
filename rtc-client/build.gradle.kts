repositories {
    maven {
        val repositoryIssueTrackingJars: String by project.parent!!
        url = uri(repositoryIssueTrackingJars)
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation(project(":framework"))
    implementation("com.ibm.team.rtc:plain-java-client:6.0.3")
    implementation("javax.activation:activation:1.1")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(testFixtures(project(":test-utils")))
}