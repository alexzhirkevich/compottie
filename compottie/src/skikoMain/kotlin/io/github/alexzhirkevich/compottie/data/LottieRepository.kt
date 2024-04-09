package io.github.alexzhirkevich.compottie.data

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

object LottieRepository {

    private val client = provideHttpClient()

    suspend fun getLottieData(url: String): String {
        val response = client.get(url)
        return response.bodyAsText()
    }
}
