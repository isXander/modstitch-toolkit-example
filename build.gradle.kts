plugins {
    `java-library`

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.neogradle) apply false

    alias(libs.plugins.modstitch.multiloader)
    alias(libs.plugins.modstitch.manifests)
    alias(libs.plugins.modstitch.modrepos)
    alias(libs.plugins.modstitch.accessx)

    alias(libs.plugins.mod.publish.plugin)
    `maven-publish`
}

group = "dev.isxander"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val canonicalAW = layout.projectDirectory.file("example.accesswidener")

// Loom does everything at configuration time which means accessx cannot be used
// to source comptime access wideners
loom.accessWidenerPath = canonicalAW

val fabricAWTask = accessx.convert("fabric", sourceSets.fabric.name) {
    inputFiles.from(canonicalAW)
    outputFormat = accessx.AW_V1
}
// modstitch-accessx: converts the access widener into an access transformer and includes it in resources
val neoforgeAWTask = accessx.convert("neoforge", sourceSets.neoforge.name) {
    inputFiles.from(canonicalAW)
    outputFormat = accessx.AT
}
// access transformers must be sourced BEFORE `neoforgeImplementation(libs.neoforge)`
accessTransformers.files.from(neoforgeAWTask.flatMap { it.outputFile })
// NeoGradle bug: https://github.com/neoforged/NeoGradle/issues/318
tasks.named { it in listOf("neoFormTransformSource", "applyAccessTransformer") }.configureEach {
    dependsOn(neoforgeAWTask)
}

repositories {
    mavenCentral()
    // modstitch-modrepos: typed, predefined modding-specific repositories
    terraformersMC()
    nucleoid()
}

dependencies {
    minecraft(libs.minecraft)
    // modstitch-multiloader: provides loader dependencies like mixin and mixin extras to common source set
    fabricLoader(libs.fabric.loader)

    libs.jackson.core.let {
        // modstitch-multiloader: common configurations apply to all source sets
        commonImplementation(it)
        commonInclude(it)
    }

    fabricImplementation(platform(libs.fabric.api.bom))
    fabricImplementation(libs.fabric.api.key.mapping.api.v1)
    fabricImplementation(libs.fabric.api.resource.loader.v1)
    fabricRuntimeOnly(libs.fabric.api)

    fabricImplementation(libs.mod.menu)
    libs.placeholder.api.let {
        fabricImplementation(it)
        // modstitch-multiloader: loader-specific JiJs are included within the universal jar
        // BUT it configures them to only load on the fabric loader
        fabricInclude(it)
    }

    neoforgeImplementation(libs.neoforge)
}

val minecraftConstraint = "[26.1,26.2)"
val supportedMinecraftVersions = manifests.minecraftReleasesMatching(minecraftConstraint)

// modstitch-manifests: programmatic fabric.mod.json and neoforge.mods.toml generation
manifests {
    // most properties are common between mod loader manifests so only need to be defined once
    // this object can be brought out of the `manifests {}` block for reference in other parts of the
    // buildscript.
    // you can also source *all* of these properties through Providers i.e., providers.gradleProperty("prop")
    val common = manifests.manifest {
        modId = "example_mod"
        version = project.version.toString()
        displayName = "Example Mod"
        description = "Does useful things"
        authors = listOf("isXander")
        sourcesUrl = "https://github.com/isXander/modstitch-toolkit-example"
        licenses = listOf("LGPL-3.0-or-later")
        iconPath = "icon.png"

        // maven-like version ranges are automatically converted to the target platform's
        // version range format, i.e., fabric's ~26.1
        dependency("minecraft", REQUIRED, minecraftConstraint)

        mixin("example_mod.mixins.json")
    }

    // Generates the defined manifests into source set's resources
    fabricModJson(sourceSets.fabric.get()) {
        from(common)

        entrypoint("main", "com.example.fabric.ExampleModFabric")

        mixin("example_mod.fabric.mixins.json")

        accessWidener(fabricAWTask)

        dependency("fabric-api", DEPENDS, libs.versions.fabric.api.get())
    }
    neoForgeModsToml(sourceSets.neoforge.get()) {
        from(common)

        mixin("example_mod.neoforge.mixins.json")

        accessTransformer(neoforgeAWTask)
    }
}

// example: using mod-publish-plugin
publishMods {
    file = tasks.universalJar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.universalSourcesJar.flatMap { it.archiveFile })

    modLoaders.addAll("fabric", "neoforge")

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        projectId = "12345678"
        minecraftVersions.addAll(supportedMinecraftVersions)
        minecraftVersions.add(loom.minecraftVersion)
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = "12345678"
        minecraftVersions.add(loom.minecraftVersion)
    }

    // --- if you would rather not use universal jars:
    val fabricOptions = publishOptions {
        file = tasks.fabricJar.flatMap { it.archiveFile }
        additionalFiles.from(tasks.fabricSourcesJar.flatMap { it.archiveFile })
        modLoaders.add("fabric")
    }
    val neoforgeOptions = publishOptions {
        file = tasks.neoforgeJar.flatMap { it.archiveFile }
        additionalFiles.from(tasks.neoforgeSourcesJar.flatMap { it.archiveFile })
        modLoaders.add("neoforge")
    }
    val modrinthOptions = modrinthOptions {
        accessToken = providers.environmentVariable("MODRINTH_API_KEY")
        projectId = "12345678"
        minecraftVersions.add(loom.minecraftVersion)
    }
    val curseforgeOptions = curseforgeOptions {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = "12345678"
        minecraftVersions.add(loom.minecraftVersion)
    }
    modrinth("modrinthFabric") {
        from(modrinthOptions, fabricOptions)
    }
    modrinth("modrinthNeoforge") {
        from(modrinthOptions, neoforgeOptions)
    }
    curseforge("curseforgeFabric") {
        from(curseforgeOptions, fabricOptions)
    }
    curseforge("curseforgeNeoforge") {
        from(curseforgeOptions, neoforgeOptions)
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            // optionally also publish universal jars
            artifact(tasks.universalJar)
            artifact(tasks.universalSourcesJar)
        }
    }

    repositories {
        mavenLocal()
    }
}
