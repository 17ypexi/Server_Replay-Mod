import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    kotlin("jvm")
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower").version("1.7.3")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    maven {
        url = uri("https://maven.parchmentmc.org/")
    }
    maven {
        url = uri("https://masa.dy.fi/maven")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    // modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    modImplementation("com.github.ReplayMod:ReplayStudio:a1e2b83") {
        exclude(group = "org.slf4j")
        exclude(group = "com.google.guava", module = "guava-jdk5")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.code.gson", module = "gson")
    }
    include(modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")!!)
    include(modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)

    modImplementation("carpet:fabric-carpet:${property("carpet_core_version")}")

    include(implementation(annotationProcessor("com.github.llamalad7.mixinextras:mixinextras-fabric:${property("mixin_extras_version")}")!!)!!)

    implementation(kotlin("stdlib-jdk8"))
}

loom {
    accessWidenerPath.set(file("src/main/resources/serverreplay.accesswidener"))
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        repositories {

        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    withSourcesJar()
}
