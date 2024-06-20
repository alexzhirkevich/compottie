package io.github.alexzhirkevich.compottie.internal

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
)
