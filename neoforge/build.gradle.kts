import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    id("com.gradleup.shadow")
}

val neoForgeVersion = providers.gradleProperty("neoforge_version").get()

val architecturyApiVersion = providers.gradleProperty("architectury_api_version").get()

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    val common by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    val shadowBundle by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    val bundledLibraries by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    named("compileClasspath") {
        extendsFrom(common, bundledLibraries)
    }
    named("runtimeClasspath") {
        extendsFrom(common, bundledLibraries)
    }
    named("developmentNeoForge") {
        extendsFrom(common, bundledLibraries)
    }
}

dependencies {
    "neoForge"("net.neoforged:neoforge:$neoForgeVersion")

    implementation("dev.architectury:architectury-neoforge:$architecturyApiVersion")

    "common"(project(":common")) {
        isTransitive = false
    }

    "shadowBundle"(project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mod.toml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.shadowJar {
    configurations = listOf(
        project.configurations.getByName("shadowBundle"),
        project.configurations.getByName("bundledLibraries")
    )

    archiveClassifier.set("")
}

tasks.sourcesJar {
    from(project(":common").sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}