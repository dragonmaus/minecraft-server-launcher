package us.dragonma.minecraft.server.launcher

import java.io.File
import java.net.URI
import java.util.Properties

internal object Server {
    internal fun run(config: Configuration, args: Array<String>): Int {
        acceptEula(config)

        val installerFile = download(config)
        val gui = if (config.server.gui) emptyList() else listOf("--nogui")
        val status: Int

        when (config.server.type) {
            ServerType.Fabric -> {
                config.log.info("Starting Fabric server")
                status = Java.runJarFile(installerFile, gui + args)
            }
            ServerType.Forge -> {
                if (!config.user.dir.resolve("libraries").exists()) {
                    config.log.info("Running Forge installer")
                    Java.runJarFile(installerFile, listOf("--installServer"))
                    config.log.info("Cleaning up after Forge installer")
                    listOf("${installerFile.name}.log", "run.bat", "run.sh").forEach {
                        val file = config.user.dir.resolve(it)
                        if (file.exists()) {
                            file.delete()
                            config.log.info2("Removed ${file.name}")
                        }
                    }
                }

                config.log.info("Starting Forge server")
                status = Java.run(
                    listOf(
                        "@user_jvm_args.txt",
                        "@libraries/net/minecraftforge/forge/${config.minecraft.version}-${config.minecraft.forge.version}/${"win" ifWindowsElse "unix"}_args.txt",
                    ) + gui + args,
                )
            }
        }

        when (status) {
            0 -> config.log.info("${config.server.type} server exited successfully")
            else -> config.log.warn("${config.server.type} server returned status $status")
        }

        return status
    }

    private fun acceptEula(config: Configuration) {
        Properties().apply {
            clear()
            set("eula", "true")
            store(config.user.dir.resolve("eula.txt").writer(), null)
        }
    }

    private fun download(config: Configuration): File {
        val file: File
        val uri: URI

        when (config.server.type) {
            ServerType.Fabric -> {
                file = config.user.dir.resolve("fabric-server-mc.${config.minecraft.version}-loader.${config.minecraft.fabric.loader.version}-launcher.${config.minecraft.fabric.installer.version}.jar")
                uri = URI("https://meta.fabricmc.net/v2/versions/loader/${config.minecraft.version}/${config.minecraft.fabric.loader.version}/${config.minecraft.fabric.installer.version}/server/jar")
            }
            ServerType.Forge -> {
                file = config.user.dir.resolve("forge-${config.minecraft.version}-${config.minecraft.forge.version}-installer.jar")
                uri = URI("https://maven.minecraftforge.net/net/minecraftforge/forge/${config.minecraft.version}-${config.minecraft.forge.version}/${file.name}")
            }
        }

        if (!file.exists()) {
            config.log.info("Downloading ${config.server.type} installer")
            uri.getFile(file)
        }
        return file
    }
}
