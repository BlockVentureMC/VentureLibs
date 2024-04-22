import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.blockventuremc"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.fruxz.dev/releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

val exposedVersion = "0.49.0"

val deps = listOf(
    "net.oneandone.reflections8:reflections8:0.11.7",
    "dev.fruxz:ascend:2024.1.2",
    "dev.fruxz:stacked:2024.1.1",
    "io.github.cdimascio:dotenv-kotlin:6.4.1",

    "org.jetbrains.exposed:exposed-core:$exposedVersion",
    "org.jetbrains.exposed:exposed-dao:$exposedVersion",
    "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
    "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
    "com.zaxxer:HikariCP:5.1.0",
    "org.mariadb.jdbc:mariadb-java-client:3.3.3",
    "io.sentry:sentry:6.17.0"
)


dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.6.25")

    deps.forEach {
        implementation(it)
        shadow(it)
    }
}


tasks {
    build {
        dependsOn("shadowJar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    withType<ProcessResources> {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("BlockVenturePlugin.jar")
    }

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}