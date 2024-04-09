package io.github.alexzhirkevich.compottie.data

import io.ktor.client.HttpClient

fun provideHttpClient(): HttpClient = HttpClient()