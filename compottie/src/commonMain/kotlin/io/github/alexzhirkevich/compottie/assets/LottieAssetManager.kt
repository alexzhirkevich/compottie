package io.github.alexzhirkevich.compottie.assets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.compottie.InternalCompottieApi

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
    suspend fun fetch(asset: LottieAsset): ByteArray?

    companion object : LottieAssetsManager {

        override suspend fun fetch(asset: LottieAsset): ByteArray? = null
    }
}


@Composable
fun rememberLottieAssetsManager(
    fetch : suspend (LottieAsset) -> ByteArray?
) : LottieAssetsManager {
    return remember { LottieAssetsManager(fetch) }
}

