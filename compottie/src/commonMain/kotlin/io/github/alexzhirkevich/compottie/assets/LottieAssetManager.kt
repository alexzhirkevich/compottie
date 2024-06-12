package io.github.alexzhirkevich.compottie.assets

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font

/**
 * Used to fetch lottie assets that are not embedded to the animation JSON file
 * */
@Stable
interface LottieAssetsManager {

    /**
     * Load image asset
     *
     * @param id unique asset id that is used for referring from animation layers
     * @param path relative system path or web URL excluding file name. For example:
     *
     * - /path/to/images/
     * - https://example.com/images/
     *
     * @param name asset name and extension (for example image.png)
     * */
    suspend fun image(image: LottieImage): ImageRepresentable?

    /**
     * Load font asset
     *
     * @param id unique asset id that is used for referring from animation layers
     * @param path relative system path or web URL excluding file name. For example:
     *
     * - /path/to/images/
     * - https://example.com/images/
     *
     * @param name asset name and extension (for example image.png)
     * */
    suspend fun font(font: LottieFont): Font?

    companion object {

        /**
         * Combine asset managers.
         *
         * Asset will be loaded by all managers one-by-one until non-null
         * asset will is received
         * */
        fun combine(
            vararg managers: LottieAssetsManager
        ) = object : LottieAssetsManager {
            override suspend fun image(image: LottieImage): ImageRepresentable? {
                return managers.firstNotNullOfOrNull { it.image(image) }
            }

            override suspend fun font(font: LottieFont): Font? {
                return managers.firstNotNullOfOrNull { it.font(font) }
            }
        }

        val Empty = object : LottieAssetsManager {

            override suspend fun image(image: LottieImage): ImageRepresentable? = null

            override suspend fun font(font: LottieFont): Font? = null
        }
    }
}

