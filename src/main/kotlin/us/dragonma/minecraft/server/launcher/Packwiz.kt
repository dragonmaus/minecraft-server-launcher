package us.dragonma.minecraft.server.launcher

import java.io.File
import java.net.URI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

internal object Packwiz {
    @OptIn(ExperimentalSerializationApi::class)
    private val lazyJson = Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    internal fun run(config: Configuration): Int {
        if (!config.packwiz.enable) {
            return 0
        }

        val installerFile = download(config)

        config.log.info("Running packwiz installer")
        return Java.runJarFile(
            installerFile,
            listOf("--no-gui", "--side", "server", config.packwiz.source.toString()),
        )
    }

    private fun download(config: Configuration): File {
        val file = config.user.dir.resolve("packwiz-installer-bootstrap.jar")

        if (!file.exists()) {
            config.log.info("Downloading packwiz installer")
            val releaseJson =
                URI(
                        "https://api.github.com/repos/packwiz/packwiz-installer-bootstrap/releases/latest"
                    )
                    .getText()
                    .body()
                    .toString()
            val releaseData = lazyJson.decodeFromString<GithubApiRelease>(releaseJson)
            URI(releaseData.assets.first { it.name == file.name }.browserDownloadUrl).getFile(file)
        }

        return file
    }

    @Serializable
    private data class GithubApiRelease(
        val url: String,
        val assetsUrl: String,
        val uploadUrl: String,
        val htmlUrl: String,
        val id: Int,
        val author: GithubApiReleaseUser,
        val nodeId: String,
        val tagName: String,
        val targetCommitish: String,
        val name: String,
        val draft: Boolean,
        val prerelease: Boolean,
        val createdAt: String,
        val publishedAt: String,
        val assets: List<GithubApiReleaseAsset>,
        val tarballUrl: String,
        val zipballUrl: String,
        val body: String,
        val reactions: GithubApiReleaseReactions,
    )

    @Serializable
    private data class GithubApiReleaseUser(
        val login: String,
        val id: Int,
        val nodeId: String,
        val avatarUrl: String,
        val gravatarId: String,
        val url: String,
        val htmlUrl: String,
        val followersUrl: String,
        val followingUrl: String,
        val gistsUrl: String,
        val starredUrl: String,
        val subscriptionsUrl: String,
        val organizationsUrl: String,
        val reposUrl: String,
        val eventsUrl: String,
        val receivedEventsUrl: String,
        val type: String,
        val userViewType: String,
        val siteAdmin: Boolean,
    )

    @Serializable
    private data class GithubApiReleaseAsset(
        val url: String,
        val id: Int,
        val nodeId: String,
        val name: String,
        val label: String?,
        val uploader: GithubApiReleaseUser,
        val contentType: String,
        val state: String,
        val size: Int,
        val downloadCount: Int,
        val createdAt: String,
        val updatedAt: String,
        val browserDownloadUrl: String,
    )

    @Serializable
    private data class GithubApiReleaseReactions(
        val url: String,
        val totalCount: Int,
        @SerialName("+1") val plusOne: Int,
        @SerialName("-1") val minusOne: Int,
        val laugh: Int,
        val hooray: Int,
        val confused: Int,
        val heart: Int,
        val rocket: Int,
        val eyes: Int,
    )
}
