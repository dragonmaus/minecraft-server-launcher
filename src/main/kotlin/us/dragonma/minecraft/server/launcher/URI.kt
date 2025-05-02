package us.dragonma.minecraft.server.launcher

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path

private fun <T> URI.get(handler: HttpResponse.BodyHandler<T>): HttpResponse<T> {
    val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()
    val request = HttpRequest.newBuilder().uri(this).build()

    return client.send(request, handler)
}

internal fun URI.getFile(file: File): HttpResponse<Path> =
    this.get(HttpResponse.BodyHandlers.ofFile(file.toPath()))

internal fun URI.getText(): HttpResponse<String> = this.get(HttpResponse.BodyHandlers.ofString())
