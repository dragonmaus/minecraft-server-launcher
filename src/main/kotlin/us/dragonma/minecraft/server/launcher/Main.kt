package us.dragonma.minecraft.server.launcher

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val config =
        try {
            Configuration("server-launcher.properties")
        } catch (e: Exception) {
            e.message?.lines()?.forEach {
                if (it.isNotBlank()) {
                    System.err.println(
                        it.trimEnd().replace("^ {4}".toRegex(), "").replace(" {4}".toRegex(), "  ")
                    )
                }
            }
            exitProcess(1)
        }

    val r = Packwiz.run(config)
    if (r != 0) {
        exitProcess(r)
    }

    exitProcess(Server.run(config, args))
}
