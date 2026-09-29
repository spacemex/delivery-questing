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

loom {
    runs {
        named("client") {
            programArguments.addAll(
                "--username",
                "DevPlayer",
                "--uuid",
                "00000000-0000-0000-0000-000000000001"
            )
        }
    }
}

fabricApi {
    configureDataGeneration() {
        client = true
        outputDirectory = project(":common").file("src/main/generated")
    }
}

repositories {
    mavenCentral()
    maven(url = uri("https://jitpack.io/"))
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

    "bundledLibraries"("org.yaml:snakeyaml:2.4")
    "bundledLibraries"("com.github.spacemex:SimpleConfigApi:manual-5")
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

    relocate("org.yaml.snakeyaml", "com.github.spacemex.deliveryquesting.libs.snakeyaml")
    relocate("com.github.spacemex.SimpleConfigApi", "com.github.spacemex.deliveryquesting.libs.simpleconfigapi")

    archiveClassifier.set("")
}

tasks.sourcesJar {
    from(project(":common").sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
