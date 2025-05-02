plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" }

internal val projectName: String by settings

rootProject.name = projectName
