package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import io.ktor.client.HttpClient
import okio.Path

/**
 * Font manager that loads fonts from the web using [request] with [client].
 *
 * Supports only [LottieFontSpec.FontOrigin.FontUrl] .ttf fonts (support may be higher on non-Android platforms).
 *
 * Note: [LottieCacheStrategy.path] should return valid file system paths to make [NetworkFontManager] work.
 * Default [DiskCacheStrategy] supports it.
 * */
fun NetworkFontManager(
    client: HttpClient = DefaultHttpClient,
    request : NetworkRequest = GetRequest,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy(),
) : LottieFontManager = NetworkFontManagerImpl(
    client = client,
    cacheStrategy = cacheStrategy,
    request = request,
)

private class NetworkFontManagerImpl(
    client: HttpClient,
    cacheStrategy: LottieCacheStrategy,
    request : NetworkRequest,
) : NetworkDownloadManager(client, cacheStrategy, request),LottieFontManager {

    override suspend fun font(font: LottieFontSpec): Font? {

        if (font.origin != LottieFontSpec.FontOrigin.FontUrl){
            return null
        }

        val (path, bytes) = load(font.path ?: return null)

        if (path == null || bytes == null){
            return null
        }

        return makeFont(font, path, bytes)
    }
}

internal expect suspend fun makeFont(spec: LottieFontSpec, path: Path, bytes: ByteArray) : Font