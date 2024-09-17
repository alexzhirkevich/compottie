@file:JvmName("CoreNetworkFontManager")


package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import okio.Path
import kotlin.jvm.JvmName

/**
 * Font manager that loads fonts from the web using [request].
 *
 * Guaranteed to work only with [LottieFontSpec.FontOrigin.FontUrl] .ttf fonts
 * (support may be higher on non-Android platforms).
 *
 * Note: [LottieCacheStrategy.path] should return valid file system paths to make [NetworkFontManager] work.
 * Default [DiskCacheStrategy] supports it.
 *
 * @param request network request used for loading fonts
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkFontManager(
    request : suspend (url: String) -> ByteArray,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieFontManager = NetworkFontManagerImpl(
    request = request,
    cacheStrategy = cacheStrategy,
)

@Stable
private class NetworkFontManagerImpl(
    private val request : suspend (url: String) -> ByteArray,
    private val cacheStrategy: LottieCacheStrategy,
) : LottieFontManager {

    override suspend fun font(font: LottieFontSpec): Font? {

        if (font.origin != LottieFontSpec.FontOrigin.FontUrl){
            return null
        }

        val (path, bytes) = networkLoad(
            request = request,
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

        if (request != other.request) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = request.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}

internal expect suspend fun makeFont(spec: LottieFontSpec, path: Path, bytes: ByteArray) : Font