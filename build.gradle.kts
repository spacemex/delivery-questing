import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT"
    id("com.gradleup.shadow") version "9.3.2" apply false
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val modName = providers.gradleProperty("archives_name").get()
val mavenGroup = providers.gradleProperty("maven_group").get()

val modVersion = providers.gradleProperty("mod_version").get()

architectury {
    minecraft = minecraftVersion
}

allprojects {
    group = mavenGroup
    version = modVersion
}

subprojects {
    apply { plugin("dev.architectury.loom-no-remap") }
    apply { plugin("architectury-plugin") }
    apply { plugin("maven-publish") }

    extensions.configure<BasePluginExtension> {
        archivesName.set("$modName-$modVersion+$minecraftVersion-${project.name}")
    }

    repositories {
        maven(url = uri("https://maven.neoforged.net/releases/"))
    }

    dependencies {
        "minecraft"("net.minecraft:minecraft:$minecraftVersion")
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        archiveVersion.set("")
    }
}
