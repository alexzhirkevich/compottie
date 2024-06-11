package io.github.alexzhirkevich.compottie.internal

import io.github.alexzhirkevich.compottie.internal.helpers.Marker
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.assets.LottieFontAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal data class LottieData(
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

    val fonts : List<LottieFontAsset>? = null,

    val markers : List<Marker> = emptyList()
)

internal val LottieData.durationMillis
    get() = (outPoint - inPoint) / frameRate * 1000