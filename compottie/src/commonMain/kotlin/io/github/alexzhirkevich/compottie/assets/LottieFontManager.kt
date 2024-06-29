package io.github.alexzhirkevich.compottie.assets

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.Font

/**
 * Used to load animation fonts. If manager returns null then glyphs or default platform font
 * will be used
 * */
@Stable
interface LottieFontManager {

    /**
     * Load [font] by requirements
     * */
    suspend fun font(font: LottieFontSpec): Font?
}