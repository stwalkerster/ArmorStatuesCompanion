import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.modrinth.minotaur)
}

version = libs.versions.armorStatuesCompanion.get()

repositories {
    mavenCentral()

    // Mojang / Fabric for MC (Loom also wires some of this, but it's safe)
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    "minecraft"(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.fabric.kotlin)
}

tasks.withType<ProcessResources>() {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

val modrinthToken: String? = System.getenv("MODRINTH_TOKEN")
if (modrinthToken != null) {
    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("Eaza1CMt")
        versionNumber.set(libs.versions.armorStatuesCompanion)
        versionType.set("release")
        uploadFile.set(tasks.jar)
        gameVersions.addAll(libs.versions.minecraft.get())
        loaders.add("fabric")
        changelog.set(rootProject.file("CHANGELOG.md").readText())
        syncBodyFrom.set(rootProject.file("README.md").readText())
        debugMode.set(true)
        dependencies {
            required.project("fabric-api")
            required.project("fabric-language-kotlin")
        }
    }
}
