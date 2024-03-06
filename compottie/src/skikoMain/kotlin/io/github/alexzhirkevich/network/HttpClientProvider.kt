package io.github.alexzhirkevich.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache

fun provideHttpClient(): HttpClient {
    return HttpClient {
        install(HttpCache)
    }
}
