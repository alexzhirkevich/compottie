package io.github.alexzhirkevich.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object LottieRepository {
    // FIXME: Use DI for HttpClient
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                }
            )
        }
    }

    suspend fun getLottieData(url: String): NetworkResult {
        return try {
            val response: HttpResponse = httpClient.get(url)
            if (response.status.value != 200) {
                return NetworkResult.Error(Exception("Failed to fetch data"))
            } else {
                val jsonString = response.body<String>()
                NetworkResult.Success(jsonString)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}