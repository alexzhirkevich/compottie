package io.github.alexzhirkevich.network

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

object LottieRepository {
    private val client = provideHttpClient()

    suspend fun getLottieData(url: String): NetworkResult {
        return try {
            val response: HttpResponse = client.get(url)
            if (response.status.isSuccess()) {
                val jsonString = response.body<String>()
                NetworkResult.Success(jsonString)
            } else {
                return NetworkResult.Error(Exception("Failed to fetch data"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}
