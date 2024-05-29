package io.github.alexzhirkevich.compottie.internal.schema

import io.github.alexzhirkevich.compottie.internal.schema.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.schema.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal class LottieData(
    @SerialName("fr")
    val frameRate : Int,

    @SerialName("w")
    val width : Int,

    @SerialName("h")
    val height : Int,

    @SerialName("v")
    val version : String,

    @SerialName("ip")
    val inPoint : Int,

    @SerialName("op")
    val outPoint : Int,

    @SerialName("nm")
    val name : String,

    val layers: List<Layer> = emptyList(),

    val assets : List<LottieAsset> = emptyList()
)

internal val LottieData.durationMillis
    get() = (outPoint - inPoint) / frameRate * 1000