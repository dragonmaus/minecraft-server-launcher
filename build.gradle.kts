plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")

	id("org.jlleitschuh.gradle.ktlint")

	application
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")
	implementation("com.sksamuel.hoplite", "hoplite-core", "2.1.1")
}

internal val projectGroup: String by project
internal val projectVersion: String by project

group = projectGroup
version = projectVersion
application.mainClass.set("us.dragonma.minecraft.server.launcher.MainKt")

internal val javaVersion: String by project

java {
	val javaVersion = JavaVersion.toVersion(javaVersion)

	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaVersion.toString().toInt()))
		sourceCompatibility = javaVersion
		targetCompatibility = javaVersion
	}
	withSourcesJar()
}

tasks.compileJava {
	options.encoding = "UTF-8"
	options.release.set(javaVersion.toInt())
	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion
}

tasks.compileKotlin {
	kotlinOptions.jvmTarget = javaVersion
}

tasks.jar {
	configurations["compileClasspath"].forEach { from(zipTree(it.absoluteFile)) }
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	from("LICENSE") { rename { "$it.${project.name}" } }
	manifest.attributes["Main-Class"] = application.mainClass
}
