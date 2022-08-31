rootProject.name = "marucs-invtweaks"
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("fabric-loom") version "1.0-SNAPSHOT"
        id("org.jetbrains.kotlin.jvm") version "1.7.10"
    }

}