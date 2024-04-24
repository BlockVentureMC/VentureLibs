import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

/**
 * Defines the versions of the dependencies inside gradle.properties.
 */
val exposedVersion: String by project
val gsonVersion: String by project
val hikariVersion: String by project
val mariadbVersion: String by project
val reflectionsVersion: String by project
val dotenvVersion: String by project
val fruxzAscendVersion: String by project
val fruxzStackedVersion: String by project
val serializationVersion: String by project
val minecraftVersion: String by project
val authlibVersion: String by project
val placeholderApiVersion: String by project

plugins {
    kotlin("jvm") version "2.0.0-RC1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.23"
    id("org.jetbrains.dokka") version  "1.9.20"
    id("org.sonarqube") version "5.0.0.4638"
    id("io.sentry.jvm.gradle") version "4.5.0"
}

sonar {
    properties {
        property("sonar.projectKey", "BlockVentureMC_BlockVenturePlugin_f0f84c38-f5bd-478b-871c-99d4b33cb318")
        property("sonar.projectName", "BlockVenturePlugin")
    }
}

sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "flawcra"
    projectName = "blockventure-plugin"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

group = "net.blockventuremc"
version = "1.0"

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

val deps = listOf(
    "net.oneandone.reflections8:reflections8:$reflectionsVersion",
    "dev.fruxz:ascend:$fruxzAscendVersion",
    "dev.fruxz:stacked:$fruxzStackedVersion",
    "io.github.cdimascio:dotenv-kotlin:$dotenvVersion",

    "org.jetbrains.exposed:exposed-core:$exposedVersion",
    "org.jetbrains.exposed:exposed-dao:$exposedVersion",
    "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
    "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
    "com.google.code.gson:gson:$gsonVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion",

    "com.zaxxer:HikariCP:$hikariVersion",
    "org.mariadb.jdbc:mariadb-java-client:$mariadbVersion",

    "dev.kord:kord-core:0.13.1",
    "dev.kord.x:emoji:0.5.0"
)


dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")

    // External dependencies
    compileOnly("com.mojang:authlib:$authlibVersion")
    compileOnly("me.clip:placeholderapi:$placeholderApiVersion")
    compileOnly("me.neznamy", "tab-api", "4.0.2")

    deps.forEach {
        implementation(it)
        shadow(it)
    }
}

open class RunSentryTask : DefaultTask() {
    init {
        group = "io.sentry"
        description = "Enables and runs the Sentry source bundling task"
    }

    @TaskAction
    fun runSentry() {
        println("Sentry task will run just before this task.")
    }
}


tasks {
    findByName("sentryBundleSourcesJava")?.enabled = false

    register<RunSentryTask>("runSentry") {
        val sentryTask = project.tasks.findByName("sentryBundleSourcesJava")
        if (sentryTask != null) {
            sentryTask.enabled = true
            dependsOn(sentryTask)
        } else {
            println("Sentry task not found")
        }
    }

    build {
        dependsOn("shadowJar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn" + "-Xopt-in=dev.kord.common.annotation.KordPreview" + "-Xopt-in=dev.kord.common.annotation.KordExperimental" + "-Xopt-in=kotlin.time.ExperimentalTime" + "-Xopt-in=kotlin.contracts.ExperimentalContracts"
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