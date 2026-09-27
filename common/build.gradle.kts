architectury {
    common(providers.gradleProperty("enabled_platforms").get().split(","))
}

sourceSets {
    main {
        resources.srcDir("src/main/generated")
    }
}
val fabricLoaderVersion = providers.gradleProperty("fabric_loader_version").get()

val architecturyApiVersion = providers.gradleProperty("architectury_api_version").get()

dependencies {
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("dev.architectury:architectury:$architecturyApiVersion")
}