package io.github.alexzhirkevich.compottie.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache

class HttpClientProvider {
    fun provideHttpClient(): HttpClient = HttpClient {
        install(HttpCache) // In-memory cache
    }
}
