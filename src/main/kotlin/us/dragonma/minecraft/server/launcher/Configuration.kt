package us.dragonma.minecraft.server.launcher

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.util.Properties

// Latest stable versions as of 2023-06-05
private const val LATEST_MINECRAFT_FABRIC_INSTALLER_VERSION = "0.10.2"
private const val LATEST_MINECRAFT_FABRIC_LOADER_VERSION = "0.14.21"
private const val LATEST_MINECRAFT_FORGE_VERSION = "45.0.66"
private const val LATEST_MINECRAFT_QUILT_INSTALLER_VERSION = "0.5.0"
private const val LATEST_MINECRAFT_QUILT_LOADER_VERSION = "0.19.0-beta.18"
private const val LATEST_MINECRAFT_VERSION = "1.19.4"

private val lazyJson = Json { ignoreUnknownKeys = true }

internal class Configuration(configFileName: String) {
	internal val log = Logger()
	internal val minecraft: MinecraftConfig
	internal val packwiz: PackwizConfig
	internal val server: ServerConfig
	internal val user: UserConfig

	private val minecraftFabricInstallerVersion: String
		get() {
			return try {
				val json = URI("https://meta.fabricmc.net/v2/versions/installer")
					.getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(FabricApiInstallerVersion.serializer()), json)

				data.find { it.stable }!!.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_FABRIC_INSTALLER_VERSION
			}
		}
	private val minecraftFabricLoaderVersion: String
		get() {
			return try {
				val json = URI("https://meta.fabricmc.net/v2/versions/loader/$minecraftVersion")
					.getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(FabricApiLoaderPayload.serializer()), json)

				data.find { it.loader.stable }!!.loader.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_FABRIC_LOADER_VERSION
			}
		}
	private val minecraftForgeVersion = LATEST_MINECRAFT_FORGE_VERSION
	private val minecraftQuiltInstallerVersion: String
		get() {
			return try {
				val json = URI("https://meta.quiltmc.org/v3/versions/installer")
					.getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(QuiltApiInstallerVersion.serializer()), json)

				data.first().version
			} catch (e: Exception) {
				LATEST_MINECRAFT_QUILT_INSTALLER_VERSION
			}
		}
	private val minecraftQuiltLoaderVersion: String
		get() {
			return try {
				val json = URI("https://meta.quiltmc.org/v3/versions/loader/$minecraftVersion")
					.getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(QuiltApiLoaderPayload.serializer()), json)

				data.first().loader.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_QUILT_LOADER_VERSION
			}
		}
	private val minecraftVersion: String
		get() {
			return try {
				val json = URI("https://meta.fabricmc.net/v2/versions/game")
					.getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(FabricApiGameVersion.serializer()), json)

				data.find { it.stable }!!.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_VERSION
			}
		}
	private val packwizEnable = false
	private val packwizSource = URI("http://www.example.com/path/to/pack.toml")
	private val serverGui = false
	private val serverType = ServerType.Fabric

	init {
		val userDir = File(System.getProperty("user.dir"))
		val configFile = userDir.resolve(configFileName)
		val config = ConfigLoaderBuilder.default()
			.addSource(PropertySource.file(configFile, true))
			.addSource(
				PropertySource.map(
					mapOf(
						"minecraft.fabric.installer.version" to minecraftFabricInstallerVersion,
						"minecraft.fabric.loader.version" to minecraftFabricLoaderVersion,
						"minecraft.forge.version" to minecraftForgeVersion,
						"minecraft.quilt.installer.version" to minecraftQuiltInstallerVersion,
						"minecraft.quilt.loader.version" to minecraftQuiltLoaderVersion,
						"minecraft.version" to minecraftVersion,
						"packwiz.enable" to packwizEnable,
						"packwiz.source" to packwizSource,
						"server.gui" to serverGui,
						"server.type" to serverType
					)
				)
			)
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
			set("minecraft.quilt.installer.version", minecraft.quilt.installer.version)
			set("minecraft.quilt.loader.version", minecraft.quilt.loader.version)
			set("minecraft.version", minecraft.version)
			set("packwiz.enable", packwiz.enable.toString())
			set("packwiz.source", packwiz.source.toString())
			set("server.gui", server.gui.toString())
			set("server.type", server.type.toString())
			store(configFile.writer(), null)
		}
	}

	@Serializable
	private class FabricApiGameVersion(val version: String, val stable: Boolean)

	@Serializable
	private class FabricApiInstallerVersion(
		// val url: String,
		// val maven: String,
		val version: String,
		val stable: Boolean
	)

	@Serializable
	private class FabricApiLoaderPayload(
		val loader: FabricApiLoaderVersion
		// val intermediary: FabricApiIntermediaryVersion,
		// val launcherMeta: FabricApiLauncherMetaVersion,
	)

	@Serializable
	private class FabricApiLoaderVersion(
		// val separator: String,
		// val build: Int,
		// val maven: String,
		val version: String,
		val stable: Boolean
	)

	@Serializable
	private class QuiltApiInstallerVersion(
		// val url: String,
		// val maven: String,
		val version: String
	)

	@Serializable
	private class QuiltApiLoaderPayload(
		val loader: QuiltApiLoaderVersion
		// val hashed: QuiltApiHashedVersion,
		// val intermediary: QuiltApiIntermediaryVersion,
		// val launcherMeta: QuiltApiLauncherMetaVersion,
	)

	@Serializable
	private class QuiltApiLoaderVersion(
		// val separator: String,
		// val build: Int,
		// val maven: String,
		val version: String
	)
}

internal data class RootConfig(
	val minecraft: MinecraftConfig,
	val packwiz: PackwizConfig,
	val server: ServerConfig
)

internal data class MinecraftConfig(
	val fabric: MinecraftFabricConfig,
	val forge: MinecraftForgeConfig,
	val quilt: MinecraftQuiltConfig,
	val version: String
)

internal data class MinecraftFabricConfig(
	val installer: MinecraftFabricInstallerConfig,
	val loader: MinecraftFabricLoaderConfig
)

internal data class MinecraftQuiltConfig(
	val installer: MinecraftQuiltInstallerConfig,
	val loader: MinecraftQuiltLoaderConfig
)

internal data class MinecraftFabricInstallerConfig(val version: String)
internal data class MinecraftFabricLoaderConfig(val version: String)
internal data class MinecraftForgeConfig(val version: String)
internal data class MinecraftQuiltInstallerConfig(val version: String)
internal data class MinecraftQuiltLoaderConfig(val version: String)
internal data class PackwizConfig(val enable: Boolean, val source: URI)
internal data class ServerConfig(val gui: Boolean, val type: ServerType)
internal data class UserConfig(val dir: File)
