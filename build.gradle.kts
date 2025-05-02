import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktfmt.gradle)
    application
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.hoplite.core)
    implementation(libs.kotlinx.serialization.json)
}

internal val projectGroup: String by project
internal val projectVersion: String by project

internal val javaVersion = libs.versions.java.get()
internal val kotlinVersion = libs.versions.kotlin.get()

group = projectGroup

version = projectVersion

application { mainClass = "$projectGroup.MainKt" }

kotlin {
    compilerOptions { jvmTarget = JvmTarget.fromTarget(javaVersion) }
    jvmToolchain(javaVersion.toInt())
}

java {
    val javaVersion_ = JavaVersion.toVersion(javaVersion)

    sourceCompatibility = javaVersion_
    targetCompatibility = javaVersion_
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        sourceCompatibility = javaVersion_
        targetCompatibility = javaVersion_
    }
    withSourcesJar()
}

tasks {
    compileJava {
        options.run {
            encoding = "UTF-8"
            release.set(javaVersion.toInt())
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    jar {
        configurations["compileClasspath"].forEach { from(zipTree(it.absoluteFile)) }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE") { rename { "$it.${inputs.properties.get("projectName")}" } }
        inputs.property("projectName", project.name)
        manifest.attributes["Main-Class"] = application.mainClass
    }
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

ktfmt { kotlinLangStyle() }
