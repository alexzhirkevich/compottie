package io.github.alexzhirkevich.compottie.internal

import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import io.github.alexzhirkevich.compottie.internal.assets.CharacterData
import io.github.alexzhirkevich.compottie.internal.assets.FontList
import io.github.alexzhirkevich.compottie.internal.helpers.Marker
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal class Animation(
    @SerialName("fr")
    val frameRate : Float,

    @SerialName("w")
    val width : Float,

    @SerialName("h")
    val height : Float,

    @SerialName("v")
    val version : String,

    @SerialName("ip")
    val inPoint : Float,

    @SerialName("op")
    val outPoint : Float,

    @SerialName("nm")
    val name : String? = null,

    val layers: List<Layer> = emptyList(),

    val assets : List<LottieAsset> = emptyList(),

    val fonts : FontList? = null,

    val chars : List<CharacterData> = emptyList(),

    val markers : List<Marker> = emptyList()
) {
    fun deepCopy() : Animation {
        return Animation(
            frameRate = frameRate,
            width = width,
            height = height,
            version = version,
            inPoint = inPoint,
            outPoint = outPoint,
            name = name,
            layers = layers.map(Layer::deepCopy),
            assets = assets.map(LottieAsset::copy),
            fonts = fonts?.deepCopy(),
            chars = chars.map(CharacterData::deepCopy),
            markers = markers
        )
    }
}
