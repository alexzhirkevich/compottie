package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url

internal val DefaultHttpClient by lazy {
    HttpClient {
        expectSuccess = true
    }
}

typealias NetworkRequest = suspend (HttpClient, Url) -> HttpResponse

internal val GetRequest : NetworkRequest by lazy {
    { c, u -> c.get(u) }
}