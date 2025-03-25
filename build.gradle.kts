plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.sshakhov.intellij"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.7")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java"))
}

tasks.patchPluginXml {
    sinceBuild.set("241")
    untilBuild.set("243.*")
}
