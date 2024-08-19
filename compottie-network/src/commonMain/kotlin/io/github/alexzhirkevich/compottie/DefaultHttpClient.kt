package io.github.alexzhirkevich.compottie

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toByteArray

internal val DefaultHttpClient by lazy {
    val ktorClient = HttpClient {
        followRedirects = true
        expectSuccess = true
        install(HttpRequestRetry) {
            maxRetries = 2
            constantDelay(250, 250)
        }
    }

    object : io.github.alexzhirkevich.compottie.network.HttpClient {
        override suspend fun get(url: String): ByteArray {
            return ktorClient.get(url).bodyAsChannel().toByteArray()
        }
    }
}
