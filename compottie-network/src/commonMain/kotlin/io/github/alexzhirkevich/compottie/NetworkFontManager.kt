package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import io.ktor.client.HttpClient
import okio.Path

/**
 * Font manager that loads fonts from the web using [request] with [client].
 *
 * Guaranteed to work only with [LottieFontSpec.FontOrigin.FontUrl] .ttf fonts
 * (support may be higher on non-Android platforms).
 *
 * Note: [LottieCacheStrategy.path] should return valid file system paths to make [NetworkFontManager] work.
 * Default [DiskCacheStrategy] supports it.
 *
 * @param client http client used for loading animation
 * @param request request builder. Simple GET by default
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@Stable
@Deprecated(
    "Use FileLoader instead of HttpClient",
    replaceWith = ReplaceWith("NetworkFontManager(fileLoader, cacheStrategy)")
)
public fun NetworkFontManager(
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieFontManager = NetworkFontManagerImpl(
    fileLoader = KtorFileLoader(client, request),
    cacheStrategy = cacheStrategy
)

/**
 * Font manager that loads fonts from the web using [fileLoader].
 *
 * Guaranteed to work only with [LottieFontSpec.FontOrigin.FontUrl] .ttf fonts
 * (support may be higher on non-Android platforms).
 *
 * Note: [LottieCacheStrategy.path] should return valid file system paths to make [NetworkFontManager] work.
 * Default [DiskCacheStrategy] supports it.
 *
 * @param fileLoader loader used for loading animation
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@Stable
public fun NetworkFontManager(
    fileLoader: FileLoader = DefaultFileLoader,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieFontManager = NetworkFontManagerImpl(
    fileLoader = fileLoader,
    cacheStrategy = cacheStrategy
)

@Stable
private class NetworkFontManagerImpl(
    private val fileLoader: FileLoader,
    private val cacheStrategy: LottieCacheStrategy
) : LottieFontManager {

    override suspend fun font(font: LottieFontSpec): Font? {

        if (font.origin != LottieFontSpec.FontOrigin.FontUrl){
            return null
        }

        val (path, bytes) = networkLoad(
            fileLoader = fileLoader,
            cacheStrategy = cacheStrategy,
            url = font.path ?: return null
        )

        if (path == null || bytes == null){
            return null
        }

        return makeFont(font, path, bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkFontManagerImpl

        if (fileLoader != other.fileLoader) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileLoader.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}

internal expect suspend fun makeFont(spec: LottieFontSpec, path: Path, bytes: ByteArray) : Font