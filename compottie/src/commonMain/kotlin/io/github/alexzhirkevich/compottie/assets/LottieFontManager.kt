package io.github.alexzhirkevich.compottie.assets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font

/**
 * Used to fetch lottie assets that are not embedded to the animation JSON file
 * */
@Stable
interface LottieFontManager {

    /**
     * Load font by [font] requirements
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

        val Empty = object : LottieFontManager {
            override suspend fun font(font: LottieFont): Font? = null
        }
    }
}