dependencies {
    implementation(project(":framework"))

    implementation(fileTree(mapOf("dir" to "../../libs", "include" to listOf("*.jar"))))
    implementation("javax.activation:activation:1.1")
    implementation("org.eclipse.platform:org.eclipse.core.runtime:3.12.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(testFixtures(project(":test-utils")))
}