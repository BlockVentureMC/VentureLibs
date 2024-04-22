import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm") version "2.0.0-RC1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.dokka") version  "1.9.20"
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
        kotlinOptions.jvmTarget = "21"
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

    withType<DokkaTask>().configureEach {
        moduleName.set(project.name)
        moduleVersion.set(project.version.toString())

        dokkaSourceSets.configureEach {
            displayName.set(name)
            jdkVersion.set(21)
            languageVersion.set("21")
            apiVersion.set("21")

            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/BlockVentureMC/BlockVenturePlugin/tree/main/src"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}