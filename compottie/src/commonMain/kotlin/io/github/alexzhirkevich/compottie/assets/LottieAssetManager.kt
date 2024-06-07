package io.github.alexzhirkevich.compottie.assets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/**
 * Used to fetch lottie assets that are not embedded to the animation JSON file
 * */
@Stable
fun interface LottieAssetsManager {

    /**
     * Fetch asset
     *
     * @param id unique asset id that is used for referring from animation layers
     * @param path relative system path or web URL excluding file name. For example:
     *
     * - /path/to/images/
     * - https://example.com/images/
     *
     * @param name asset name and extension (for example image.png)
     * */
    suspend fun fetch(id: String, path: String, name: String): ByteArray?

    companion object {
        fun Compound(
            networkFetcher: LottieAssetsManager,
            localFetcher: LottieAssetsManager,
            cache: LottieAssetsCache
        ): LottieAssetsManager = CompoundLottieAssetsFetcher(networkFetcher, localFetcher, cache)
    }
}


@Composable
fun rememberLottieAssetsManager(
    fetch : suspend (id: String, path: String, name: String) -> ByteArray
) : LottieAssetsManager {
    return remember { LottieAssetsManager(fetch) }
}

internal val NoOpAssetsManager = LottieAssetsManager { _, _, _ -> null }


