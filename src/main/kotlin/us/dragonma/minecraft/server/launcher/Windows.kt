package us.dragonma.minecraft.server.launcher

private val isWindows by lazy { System.getProperty("os.name").lowercase().contains("win") }

internal infix fun String.ifWindowsElse(s: String): String = if (isWindows) this else s
