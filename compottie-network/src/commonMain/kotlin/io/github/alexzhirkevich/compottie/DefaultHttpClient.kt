package io.github.alexzhirkevich.compottie

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.utils.io.InternalAPI

internal val DefaultHttpClient by lazy {
    HttpClient {
        followRedirects = true
        expectSuccess = true
        install(HttpTimeout){
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 15_000
        }
        install(HttpRequestRetry) {
            maxRetries = 2
            exponentialDelay()
        }
    }
}

@OptIn(InternalAPI::class)
internal val DefaultHttpRequest : suspend (String) -> ByteArray = {
    DefaultHttpClient.get(it).bodyAsBytes()
}