plugins {
    idea
    java
    id("com.github.johnrengelman.shadow")
    id("xyz.wagyourtail.unimined") version "1.1.0-SNAPSHOT"
    kotlin("jvm") version (libs.versions.kotlinVersion)

}

java {
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}
/*

loom {
    log4jConfigs.from(rootProject.file("log4j2.xml"))
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "io.github.moulberry.moulconfig.tweaker.DevelopmentResourceTweaker")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            arg("--mixin", "mixins.eggsentials.json")
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.eggsentials.json")
    }
    mixin {
        defaultRefmapName.set("mixins.eggsentials.refmap.json")
    }
}

*/

unimined.minecraft {
    version("1.8.9")
    mappings { searge();mcp("stable", "22-1.8.9") }
    minecraftForge { loader("11.15.1.2318-1.8.9") }
    runs {
        config("client") {
            jvmArgs.add("-Dmixin.debug=true")
            jvmArgs.add("-Dmoulconfig.warn.crash=false")
            args.addAll(
                listOf(
                    "--tweakClass", "org.spongepowered.asm.launch.MixinTweaker",
                    "--tweakClass", "io.github.moulberry.moulconfig.tweaker.DevelopmentResourceTweaker",
                    "--mixin", "mixins.eggsentials.json",
                )
            )
        }
    }
}


repositories {
    mavenCentral()

}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
    isTransitive = false
}

val shadowModImpl: Configuration by configurations.creating {
    configurations.getByName("modImplementation").extendsFrom(this)
    isTransitive = false
}

dependencies {
    shadowImpl(libs.legacy.mixin.rt)
    shadowImpl(project(":common"))
    //annotationProcessor(libs.legacy.mixin.processor)

    implementation(libs.legacy.devauth)
    shadowModImpl(libs.discordJDA)

    "shadowModImpl"(libs.legacy.moulconfig)
}

tasks.withType(Jar::class) {
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.eggsentials.json"
    }
}

tasks.processResources {
    from(project(":common").sourceSets["main"].resources.srcDirs)

    //inputs.property("mod_version", libs.versions.modVersion)
    inputs.property("mod_version", libs.versions.modVersion)

    filesMatching(listOf("mcmod.info", "mixins.eggsentials.json")) {
        expand(inputs.properties)
    }

    rename("(.+_at.cfg)", "META-INF/$1")
    from(project(":common").sourceSets["main"].resources.srcDirs)
}


val remapJar by tasks.named<xyz.wagyourtail.unimined.api.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}
tasks.compileJava {
    dependsOn(tasks.processResources)
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl, shadowModImpl)
    relocate("io.github.moulberry.moulconfig", "de.hype.eggsentials.deps.moulconfig")
    relocate("net.dv8tion", "de.hype.eggsentials.deps.dcJDA")

}
tasks.assemble.get().dependsOn(remapJar)
