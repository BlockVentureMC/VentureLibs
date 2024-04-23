import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm") version "2.0.0-RC1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.dokka") version  "1.9.20"
    id("org.sonarqube") version "5.0.0.4638"
    id("io.sentry.jvm.gradle") version "3.12.0"
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
    "com.google.code.gson:gson:2.10.1",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2",


    "com.zaxxer:HikariCP:5.1.0",
    "org.mariadb.jdbc:mariadb-java-client:3.3.3",
)


dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // External dependencies
    compileOnly("com.mojang:authlib:1.6.25")
    compileOnly("me.clip:placeholderapi:2.11.5")

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