import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.asciidoctor.jvm.convert") version "3.3.1"
    id("com.github.jruby-gradle.base") version "0.1.5"
    id("org.asciidoctor.jvm.gems") version "3.1.0"
}

group = "ch.loewenfels.issuetrackingsync"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
}

dependencies {
    asciidoctorGems("rubygems:asciidoctor-diagram:1.2.0")
}

asciidoctorj {
    requires("asciidoctor-diagram")
}

tasks.withType<AsciidoctorTask> {
    dependsOn("jrubyPrepareGems")
    sourceDir("asciidoc")
}

defaultTasks("asciidoctor")