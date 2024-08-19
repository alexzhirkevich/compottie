package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import io.github.alexzhirkevich.compottie.network.DiskCacheStrategy
import io.github.alexzhirkevich.compottie.network.HttpClient
import io.github.alexzhirkevich.compottie.network.LottieCacheStrategy

/**
 * Font manager that loads fonts from the web using [client].
 *
 * Guaranteed to work only with [LottieFontSpec.FontOrigin.FontUrl] .ttf fonts
 * (support may be higher on non-Android platforms).
 *
 * Note: [LottieCacheStrategy.path] should return valid file system paths to make [NetworkFontManager] work.
 * Default [DiskCacheStrategy] supports it.
 *
 * @param client http client used for loading animation
 * @param cacheStrategy caching strategy. Caching to system temp dir by default
 * */
@OptIn(InternalCompottieApi::class)
@Stable
public fun NetworkFontManager(
    client: HttpClient = DefaultHttpClient,
    cacheStrategy: LottieCacheStrategy = DiskCacheStrategy.Instance,
) : LottieFontManager = io.github.alexzhirkevich.compottie.network.NetworkFontManager(client,)

