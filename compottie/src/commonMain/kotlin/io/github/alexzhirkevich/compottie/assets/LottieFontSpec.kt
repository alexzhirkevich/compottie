package io.github.alexzhirkevich.compottie.assets

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

class LottieFontSpec internal constructor(
    val family : String,
    val name : String,
    val style : FontStyle,
    val weight : FontWeight,
    val path : String?,
    val origin: FontOrigin
) {
    enum class FontOrigin {
        /**
         * Font from assets or file system
         * */
        Local,

        /**
         * Css font from url
         *
         * Example: [https://fonts.googleapis.com/css2?family=Poppins:wght@700&display=swap](https://fonts.googleapis.com/css2?family=Poppins:wght@700&display=swap)
         * */
        CssUrl,


        ScriptUrl,

        /**
         * .ttf font from url
         *
         * Example: [https://fonts.gstatic.com/s/ubuntu/v15/4iCp6KVjbNBYlgoKejZftWyI.ttf](https://fonts.gstatic.com/s/ubuntu/v15/4iCp6KVjbNBYlgoKejZftWyI.ttf)
         * */
        FontUrl,

        /**
         * Font from unknown source
         *
         * Example: [https://fonts.gstatic.com/s/ubuntu/v15/4iCp6KVjbNBYlgoKejZftWyI.ttf](https://fonts.gstatic.com/s/ubuntu/v15/4iCp6KVjbNBYlgoKejZftWyI.ttf)
         * */
        Unknown
    }
}