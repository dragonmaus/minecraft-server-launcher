package us.dragonma.minecraft.server.launcher

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import java.io.File
import java.net.URI
import java.util.Properties

internal class Configuration(configFileName: String) {
    internal val log = Logger()
    internal val minecraft: MinecraftConfig
    internal val packwiz: PackwizConfig
    internal val server: ServerConfig
    internal val user: UserConfig

    init {
        val userDir = File(System.getProperty("user.dir"))
        val configFile = userDir.resolve(configFileName)
        val config = ConfigLoaderBuilder.default()
            .addSource(PropertySource.file(configFile, true))
            .addSource(PropertySource.resource("/default.properties"))
            .build()
            .loadConfigOrThrow<RootConfig>()

        minecraft = config.minecraft
        packwiz = config.packwiz
        server = config.server
        user = UserConfig(userDir)

        Properties().apply {
            clear()
            set("minecraft.fabric.installer.version", minecraft.fabric.installer.version)
            set("minecraft.fabric.loader.version", minecraft.fabric.loader.version)
            set("minecraft.forge.version", minecraft.forge.version)
            set("minecraft.version", minecraft.version)
            set("packwiz.enable", packwiz.enable.toString())
            set("packwiz.source", packwiz.source.toString())
            set("server.gui", server.gui.toString())
            set("server.type", server.type.toString())
            store(configFile.writer(), null)
        }
    }
}

internal data class RootConfig(
    val minecraft: MinecraftConfig,
    val packwiz: PackwizConfig,
    val server: ServerConfig,
)

internal data class MinecraftConfig(
    val fabric: MinecraftFabricConfig,
    val forge: MinecraftForgeConfig,
    val version: String,
)

internal data class MinecraftFabricConfig(
    val installer: MinecraftFabricInstallerConfig,
    val loader: MinecraftFabricLoaderConfig,
)

internal data class MinecraftFabricInstallerConfig(val version: String)
internal data class MinecraftFabricLoaderConfig(val version: String)
internal data class MinecraftForgeConfig(val version: String)
internal data class PackwizConfig(val enable: Boolean, val source: URI)
internal data class ServerConfig(val gui: Boolean, val type: ServerType)
internal data class UserConfig(val dir: File)
