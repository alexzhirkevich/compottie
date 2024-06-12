package io.github.alexzhirkevich.compottie.internal.assets

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class FontList(
    val list : List<LottieFontAsset>
)

@Serializable
internal class LottieFontAsset(

    @SerialName("fFamily")
    val family : String,

    @SerialName("fName")
    val name : String,

    @SerialName("fStyle")
    val style : String,

    @SerialName("fPath")
    val path : String? = null
) {

    @Transient
    private val lStyle = style.lowercase()

    @Transient
    val weight = when {
        "extra" in lStyle && "light" in lStyle -> FontWeight.ExtraLight
        "light" in lStyle -> FontWeight.Light
        "extra" in lStyle && "bold" in lStyle -> FontWeight.ExtraBold
        "semi" in style && "bold" in lStyle -> FontWeight.SemiBold
        "bold" in lStyle -> FontWeight.Bold
        "black" in lStyle -> FontWeight.Black
        else -> FontWeight.Normal
    }

    @Transient
    val fontStyle = if ("italic" in lStyle)
        FontStyle.Italic else FontStyle.Normal

    var font : Font? = null
}