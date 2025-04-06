package us.dragonma.minecraft.server.launcher

import java.io.File

internal object Java {
	private val javaCommand = File(System.getProperty("java.home")).resolve("bin").resolve("java.exe" ifWindowsElse "java").toString()

	internal fun run(args: List<String>): Int = ProcessBuilder(listOf(javaCommand) + args).inheritIO().start().waitFor()

	internal fun runJarFile(
		file: File,
		args: List<String>,
	): Int = this.run(listOf("-jar", file.toString()) + args)
}
