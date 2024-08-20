package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.util.toByteArray

internal val DefaultFileLoader by lazy {
    KtorFileLoader(DefaultHttpClient, GetRequest)
}

@Stable
public class KtorFileLoader(
    private val client: HttpClient,
    private val request: NetworkRequest
) : FileLoader {

    public override suspend fun load(url: String): ByteArray? {
        val ktorUrl = try {
            Url(url)
        } catch (t: URLParserException) {
            return null
        }

        return request(client, ktorUrl).execute().bodyAsChannel().toByteArray()
    }
}