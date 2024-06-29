package io.github.alexzhirkevich.compottie

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.prepareGet
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.HttpStatement
import io.ktor.http.Url

internal val DefaultHttpClient by lazy {
    HttpClient {
        followRedirects = true
        expectSuccess = true
        install(HttpRequestRetry) {
            maxRetries = 2
            constantDelay(250, 250)
        }
    }
}

/**
 * Http request builder.
 *
 * See [HttpClient.prepareRequest], [HttpClient.prepareGet],
 * */
typealias NetworkRequest = suspend (HttpClient, Url) -> HttpStatement

internal val GetRequest : NetworkRequest = { c, u -> c.prepareGet(u) }