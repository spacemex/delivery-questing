pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
        mavenCentral()
    }
}

rootProject.name = "example-mod"

include("common")
include("fabric")
include("neoforge")
