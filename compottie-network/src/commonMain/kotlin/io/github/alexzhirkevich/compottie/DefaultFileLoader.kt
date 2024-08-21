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

/**
 * Default [FileLoader] that uses ktor to as http client
 *
 * @param client http client used for loading animations, assets and fonts
 * @param request request builder. Simple GET by default
 * */
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KtorFileLoader

        if (client != other.client) return false
        if (request != other.request) return false

        return true
    }

    override fun hashCode(): Int {
        var result = client.hashCode()
        result = 31 * result + request.hashCode()
        return result
    }
}