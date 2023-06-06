internal val projectName: String by settings
rootProject.name = projectName

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		val kotlinVersion: String by settings
		kotlin("jvm") version kotlinVersion
		kotlin("plugin.serialization") version kotlinVersion

		val ktlintVersion: String by settings
		id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
	}
}
