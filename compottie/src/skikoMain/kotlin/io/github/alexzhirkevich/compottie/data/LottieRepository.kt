package io.github.alexzhirkevich.compottie.data

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

object LottieRepository {

    private val httpClient = HttpClientProvider().provideHttpClient()

    suspend fun getLottieData(url: String): String {
        return httpClient.get(url).bodyAsText()
    }
}
