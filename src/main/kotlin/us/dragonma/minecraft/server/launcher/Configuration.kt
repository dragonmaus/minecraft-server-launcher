package us.dragonma.minecraft.server.launcher

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.util.Properties

// Latest stable versions as of 2025-04-05
private const val LATEST_MINECRAFT_FABRIC_INSTALLER_VERSION = "1.0.3"
private const val LATEST_MINECRAFT_FABRIC_LOADER_VERSION = "0.16.12"
private const val LATEST_MINECRAFT_FORGE_VERSION = "55.0.3"
private const val LATEST_MINECRAFT_NEOFORGE_VERSION = "21.5.26-beta"
private const val LATEST_MINECRAFT_QUILT_INSTALLER_VERSION = "0.12.1"
private const val LATEST_MINECRAFT_QUILT_LOADER_VERSION = "0.29.0-beta.3"
private const val LATEST_MINECRAFT_VERSION = "1.21.5"

private val lazyJson = Json { ignoreUnknownKeys = true }

internal class Configuration(
	configFileName: String,
) {
	internal val log = Logger()
	internal val minecraft: MinecraftConfig
	internal val packwiz: PackwizConfig
	internal val server: ServerConfig
	internal val user: UserConfig

	private val minecraftFabricInstallerVersion: String
		get() {
			return try {
				val json = URI("https://meta.fabricmc.net/v2/versions/installer").getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(FabricApiInstallerVersion.serializer()), json)

				data.find { it.stable }!!.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_FABRIC_INSTALLER_VERSION
			}
		}

	private val minecraftFabricLoaderVersion: String
		get() {
			return try {
				val json = URI("https://meta.fabricmc.net/v2/versions/loader/$minecraftVersion").getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(FabricApiLoaderVersion.serializer()), json)

				data.find { it.loader.stable }!!.loader.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_FABRIC_LOADER_VERSION
			}
		}

	private val minecraftForgeVersion: String
		get() {
			return try {
				val json = URI("https://mc-versions-api.net/api/forge").getText().body().toString()
				val data = lazyJson.decodeFromString<ForgeApiVersion>(json)

				data.result.first()[minecraftVersion]!!.first()
			} catch (e: Exception) {
				LATEST_MINECRAFT_FORGE_VERSION
			}
		}

	private val minecraftNeoForgeVersion: String
		get() {
			return try {
				val json = URI("https://maven.neoforged.net/api/maven/latest/version/releases/net%2Fneoforged%2Fneoforge").getText().body().toString()
				val data = lazyJson.decodeFromString<NeoForgeApiVersion>(json)

				data.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_NEOFORGE_VERSION
			}
		}

	private val minecraftQuiltInstallerVersion: String
		get() {
			return try {
				val json = URI("https://meta.quiltmc.org/v3/versions/installer").getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(QuiltApiInstallerVersion.serializer()), json)

				data.first().version
			} catch (e: Exception) {
				LATEST_MINECRAFT_QUILT_INSTALLER_VERSION
			}
		}

	private val minecraftQuiltLoaderVersion: String
		get() {
			return try {
				val json = URI("https://meta.quiltmc.org/v3/versions/loader/$minecraftVersion").getText().body().toString()
				val data = lazyJson.decodeFromString(ListSerializer(QuiltApiLoaderVersion.serializer()), json)

				data.first().loader.version
			} catch (e: Exception) {
				LATEST_MINECRAFT_QUILT_LOADER_VERSION
			}
		}

	private val minecraftVersion: String
		get() {
			return try {
				val json = URI("https://mc-versions-api.net/api/java").getText().body().toString()
				val data = lazyJson.decodeFromString<MinecraftApiVersion>(json)

				data.result.first()
			} catch (e: Exception) {
				LATEST_MINECRAFT_VERSION
			}
		}

	private val packwizEnable = false
	private val packwizSource = URI("http://www.example.com/path/to/pack.toml")
	private val serverGui = false
	private val serverType = ServerType.NeoForge

	init {
		val userDir = File(System.getProperty("user.dir"))
		val configFile = userDir.resolve(configFileName)
		val config =
			ConfigLoaderBuilder
				.default()
				.addSource(PropertySource.file(configFile, true))
				.addSource(
					PropertySource.map(
						mapOf(
							"minecraft.fabric.installer.version" to minecraftFabricInstallerVersion,
							"minecraft.fabric.loader.version" to minecraftFabricLoaderVersion,
							"minecraft.forge.version" to minecraftForgeVersion,
							"minecraft.neoforge.version" to minecraftNeoForgeVersion,
							"minecraft.quilt.installer.version" to minecraftQuiltInstallerVersion,
							"minecraft.quilt.loader.version" to minecraftQuiltLoaderVersion,
							"minecraft.version" to minecraftVersion,
							"packwiz.enable" to packwizEnable,
							"packwiz.source" to packwizSource,
							"server.gui" to serverGui,
							"server.type" to serverType,
						),
					),
				).build()
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
			set("minecraft.neoforge.version", minecraft.neoforge.version)
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
	private data class FabricApiInstallerVersion(
		val url: String,
		val maven: String,
		val version: String,
		val stable: Boolean,
	)

	@Serializable
	private data class FabricApiLoaderVersion(
		val loader: FabricApiLoaderVersionLoader,
		val intermediary: FabricApiLoaderVersionIntermediary,
		val launcherMeta: FabricApiLoaderVersionLauncherMeta,
	)

	@Serializable
	private data class FabricApiLoaderVersionIntermediary(
		val maven: String,
		val version: String,
		val stable: Boolean,
	)

	@Serializable
	private data class FabricApiLoaderVersionLauncherMeta(
		val version: Int,
		@SerialName("min_java_version") val minJavaVersion: Int,
		val libraries: FabricApiLoaderVersionLauncherMetaLibraries,
		val mainClass: FabricApiLoaderVersionLauncherMetaMainClass,
	)

	@Serializable
	private data class FabricApiLoaderVersionLauncherMetaLibraries(
		val client: List<FabricApiLoaderVersionLauncherMetaLibrary>,
		val common: List<FabricApiLoaderVersionLauncherMetaLibrary>,
		val server: List<FabricApiLoaderVersionLauncherMetaLibrary>,
		val development: List<FabricApiLoaderVersionLauncherMetaLibrary>,
	)

	@Serializable
	private data class FabricApiLoaderVersionLauncherMetaLibrary(
		val name: String,
		val url: String,
		val md5: String,
		val sha1: String,
		val sha256: String,
		val sha512: String,
		val size: Int,
	)

	@Serializable
	private data class FabricApiLoaderVersionLauncherMetaMainClass(
		val client: String,
		val server: String,
	)

	@Serializable
	private data class FabricApiLoaderVersionLoader(
		val separator: String,
		val build: Int,
		val maven: String,
		val version: String,
		val stable: Boolean,
	)

	@Serializable
	private data class ForgeApiVersion(
		val result: List<Map<String, List<String>>>,
	)

	@Serializable
	private data class MinecraftApiVersion(
		val result: List<String>,
	)

	@Serializable
	private data class NeoForgeApiVersion(
		val isSnapshot: Boolean,
		val version: String,
	)

	@Serializable
	private data class QuiltApiInstallerVersion(
		val url: String,
		val maven: String,
		val version: String,
	)

	@Serializable
	private data class QuiltApiLoaderVersion(
		val loader: QuiltApiLoaderVersionLoader,
		val hashed: QuiltApiLoaderVersionHashed,
		val intermediary: QuiltApiLoaderVersionIntermediary,
		val launcherMeta: QuiltApiLoaderVersionLauncherMeta,
	)

	@Serializable
	private data class QuiltApiLoaderVersionHashed(
		val maven: String,
		val version: String,
	)

	@Serializable
	private data class QuiltApiLoaderVersionIntermediary(
		val maven: String,
		val version: String,
	)

	@Serializable
	private data class QuiltApiLoaderVersionLauncherMeta(
		val version: Int,
		@SerialName("min_java_version") val minJavaVersion: Int,
		val libraries: QuiltApiLoaderVersionLauncherMetaLibraries,
		val mainClass: QuiltApiLoaderVersionLauncherMetaMainClass,
	)

	@Serializable
	private data class QuiltApiLoaderVersionLauncherMetaLibraries(
		val client: List<QuiltApiLoaderVersionLauncherMetaLibrary>,
		val common: List<QuiltApiLoaderVersionLauncherMetaLibrary>,
		val server: List<QuiltApiLoaderVersionLauncherMetaLibrary>,
		val development: List<QuiltApiLoaderVersionLauncherMetaLibrary>,
	)

	@Serializable
	private data class QuiltApiLoaderVersionLauncherMetaLibrary(
		val name: String,
		val url: String,
	)

	@Serializable
	private data class QuiltApiLoaderVersionLauncherMetaMainClass(
		val client: String,
		val server: String,
		val serverLauncher: String,
	)

	@Serializable
	private data class QuiltApiLoaderVersionLoader(
		val separator: String,
		val build: Int,
		val maven: String,
		val version: String,
	)
}

internal data class MinecraftConfig(
	val fabric: MinecraftFabricConfig,
	val forge: MinecraftForgeConfig,
	val neoforge: MinecraftNeoForgeConfig,
	val quilt: MinecraftQuiltConfig,
	val version: String,
)

internal data class MinecraftFabricConfig(
	val installer: MinecraftFabricInstallerConfig,
	val loader: MinecraftFabricLoaderConfig,
)

internal data class MinecraftFabricInstallerConfig(
	val version: String,
)

internal data class MinecraftFabricLoaderConfig(
	val version: String,
)

internal data class MinecraftForgeConfig(
	val version: String,
)

internal data class MinecraftNeoForgeConfig(
	val version: String,
)

internal data class MinecraftQuiltConfig(
	val installer: MinecraftQuiltInstallerConfig,
	val loader: MinecraftQuiltLoaderConfig,
)

internal data class MinecraftQuiltInstallerConfig(
	val version: String,
)

internal data class MinecraftQuiltLoaderConfig(
	val version: String,
)

internal data class PackwizConfig(
	val enable: Boolean,
	val source: URI,
)

internal data class RootConfig(
	val minecraft: MinecraftConfig,
	val packwiz: PackwizConfig,
	val server: ServerConfig,
)

internal data class ServerConfig(
	val gui: Boolean,
	val type: ServerType,
)

internal data class UserConfig(
	val dir: File,
)
