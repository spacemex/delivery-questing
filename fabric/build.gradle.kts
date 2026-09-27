plugins {
    id("com.gradleup.shadow")
}

val fabricLoaderVersion = providers.gradleProperty("fabric_loader_version").get()
val fabricApiVersion = providers.gradleProperty("fabric_api_version").get()

val architecturyApiVersion = providers.gradleProperty("architectury_api_version").get()

architectury {
    platformSetupLoomIde()
    fabric()
}

fabricApi {
    configureDataGeneration() {
        client = true
        outputDirectory = project(":common").file("src/main/generated")
    }
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
    named("developmentFabric") {
        extendsFrom(common, bundledLibraries)
    }
}

dependencies {
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("dev.architectury:architectury-fabric:$architecturyApiVersion")

    "common"(project(":common")) {
        isTransitive = false
    }

    "shadowBundle"(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
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
