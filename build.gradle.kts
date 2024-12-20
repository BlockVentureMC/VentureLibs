import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

/**
 * Defines the versions of the dependencies inside gradle.properties.
 */
val exposedVersion: String by project
val gsonVersion: String by project
val hikariVersion: String by project
val mariadbVersion: String by project
val dotenvVersion: String by project
val fruxzAscendVersion: String by project
val fruxzStackedVersion: String by project
val serializationVersion: String by project
val minecraftVersion: String by project
val authlibVersion: String by project
val placeholderApiVersion: String by project
val audioServerVersion: String by project
val sentryVersion: String by project
val jdaVersion: String by project
val smoothCoastersAPIVersion: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.4"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.sonarqube") version "5.0.0.4638"
    id("io.papermc.paperweight.userdev") version "1.7.4"
    //id("io.sentry.jvm.gradle") version "4.6.0"
}

sonar {
    properties {
        property("sonar.projectKey", "BlockVentureMC_BlockVenturePlugin_f0f84c38-f5bd-478b-871c-99d4b33cb318")
        property("sonar.projectName", "BlockVenturePlugin")
    }
}
/*
sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "flawcra"
    projectName = "blockventure-plugin"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")

    autoInstallation {
        enabled = true
    }
}
 */

group = "net.blockventuremc"
version = "1.0"

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

val deps = listOf(
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
    "net.blockventuremc.audioserver:minecraft:$audioServerVersion",

    "io.sentry:sentry:$sentryVersion",
    "io.sentry:sentry-kotlin-extensions:$sentryVersion",
    "io.sentry:sentry-logback:$sentryVersion",

    "net.dv8tion:JDA:$jdaVersion",
    "me.m56738:SmoothCoastersAPI:$smoothCoastersAPIVersion",
)

val includedDependencies = mutableListOf<String>()

fun Dependency?.deliver() = this?.apply {
    val computedVersion = version ?: kotlin.coreLibrariesVersion
    includedDependencies += "${group}:${name}:${computedVersion}"
}

dependencies {
    paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT")

    compileOnly("net.blockventuremc.audioserver:common:$audioServerVersion")

    // reflections
    implementation(kotlin("stdlib")).deliver()
    implementation(kotlin("reflect")).deliver()

    // External dependencies
    compileOnly("com.mojang:authlib:$authlibVersion")
    compileOnly("me.clip:placeholderapi:$placeholderApiVersion")
    compileOnly("net.luckperms", "api", "5.4")

    deps.forEach {
        implementation(it).deliver()
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

tasks.register("generateDependenciesFile") {
    group = "build"
    description = "Writes dependencies to file"

    val dependenciesFile = File(layout.buildDirectory.asFile.get(), "generated-resources/.dependencies")
    outputs.file(dependenciesFile)
    doLast {
        dependenciesFile.parentFile.mkdirs()
        dependenciesFile.writeText(includedDependencies.joinToString("\n"))
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
        dependsOn(shadowJar)
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.getByName("shadow"))
        archiveFileName.set("VentureLibs.jar")
    }

    withType<ProcessResources> {
        dependsOn("generateDependenciesFile")

        from(File(layout.buildDirectory.asFile.get(), "generated-resources")) {
            include(".dependencies")
        }

        expand(
            "version" to project.version,
            "name" to project.name,
            "vendeps" to includedDependencies.joinToString("\n"),
        )
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
                remoteUrl.set(URI("https://github.com/BlockVentureMC/VentureLibs/tree/main/src").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}

configure<SourceSetContainer> { // allowing java files appearing next to kotlin files
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.time.ExperimentalTime",
                "-Xopt-in=kotlin.contracts.ExperimentalContracts"
            )
        )
    }
}