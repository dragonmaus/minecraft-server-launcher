package us.dragonma.minecraft.server.launcher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI

internal object Packwiz {
	private val lazyJson = Json { ignoreUnknownKeys = true }

	internal fun run(config: Configuration): Int {
		if (!config.packwiz.enable) return 0

		val installerFile = download(config)

		config.log.info("Running packwiz installer")
		return Java.runJarFile(installerFile, listOf("--no-gui", "--side", "server", config.packwiz.source.toString()))
	}

	private fun download(config: Configuration): File {
		val file = config.user.dir.resolve("packwiz-installer-bootstrap.jar")

		if (!file.exists()) {
			config.log.info("Downloading packwiz installer")
			val releaseJson = URI("https://api.github.com/repos/packwiz/packwiz-installer-bootstrap/releases/latest")
				.getText().body().toString()
			val releaseData = lazyJson.decodeFromString<GitHubRelease>(releaseJson)
			URI(releaseData.assets.filter { it.name == file.name }[0].browserDownloadUrl)
				.getFile(file)
		}

		return file
	}

	@Serializable
	private class GitHubRelease(val assets: List<GitHubReleaseAsset>)

	@Serializable
	private class GitHubReleaseAsset(val name: String, @SerialName("browser_download_url") val browserDownloadUrl: String)
}
