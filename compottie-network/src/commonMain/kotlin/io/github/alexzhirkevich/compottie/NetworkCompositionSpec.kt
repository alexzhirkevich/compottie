import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.DefaultHttpClient
import io.github.alexzhirkevich.compottie.DiskCacheStrategy
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.GetRequest
import io.github.alexzhirkevich.compottie.L
import io.github.alexzhirkevich.compottie.LottieAnimationFormat
import io.github.alexzhirkevich.compottie.LottieCacheStrategy
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.NetworkAssetsManager
import io.github.alexzhirkevich.compottie.NetworkRequest
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.rememberNetworkAssetsManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.util.toByteArray

/**
 * [LottieComposition] from web [url]
 *
 * @param client Ktor http client to use
 * @param assetsManager lottie assets manager. By default no-op manager is used.
 * Use [NetworkAssetsManager] if assets use web URLs too
 *
 * @see rememberNetworkAssetsManager
 * */
@Stable
fun LottieCompositionSpec.Companion.Url(
    url : String,
    format: LottieAnimationFormat = LottieAnimationFormat.Unknown,
    client: HttpClient = DefaultHttpClient,
    assetsManager: LottieAssetsManager = LottieAssetsManager,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
    request : NetworkRequest = GetRequest,
) : LottieCompositionSpec = NetworkCompositionSpec(
    url = url,
    format = format,
    client = client,
    assetsManager = assetsManager,
    cacheStrategy = cacheStrategy,
    request = request
)

@Stable
private class NetworkCompositionSpec(
    private val url : String,
    private val format: LottieAnimationFormat,
    private val client : HttpClient,
    private val assetsManager: LottieAssetsManager,
    private val cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
    private val request : NetworkRequest,
) : LottieCompositionSpec {

    override suspend fun load(): LottieComposition {

        cacheStrategy.load(url)?.let {
            val delegate = if (byteArrayOf(it[0]).decodeToString() == "{") {
                LottieCompositionSpec.JsonString(assetsManager) { it.decodeToString() }
            } else {
                LottieCompositionSpec.DotLottie(assetsManager) { it }
            }

            return delegate.load()
        }

        val resp = request(this.client, Url(url))

        if (!resp.status.isSuccess()) {
            throw ClientRequestException(resp, resp.bodyAsText())
        }

        val contentType = resp.headers[HttpHeaders.ContentType]?.lowercase()

        val isJson = format == LottieAnimationFormat.Json ||
                contentType == null ||
                contentType == "application/json" ||
                contentType.startsWith("text")

        val bytes = resp.bodyAsChannel().toByteArray()

        val delegate = if (isJson) {
            LottieCompositionSpec.JsonString(assetsManager) { bytes.decodeToString() }
        } else {
            LottieCompositionSpec.DotLottie(assetsManager) { bytes }
        }

        val composition = delegate.load()

        try {
            cacheStrategy.save(url, bytes)
        } catch (t: Throwable) {
            L.logger.error("Lottie disk cache strategy error", t)
        }
        return composition
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkCompositionSpec

        if (url != other.url) return false
        if (format != other.format) return false
        if (client != other.client) return false
        if (assetsManager != other.assetsManager) return false
        if (cacheStrategy != other.cacheStrategy) return false
        if (request != other.request) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + client.hashCode()
        result = 31 * result + assetsManager.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        result = 31 * result + request.hashCode()
        return result
    }
}