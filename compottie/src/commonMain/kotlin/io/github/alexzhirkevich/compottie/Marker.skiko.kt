package io.github.alexzhirkevich.compottie

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Marker(
    @SerialName("tm")
    val startFrame: Float,
    @SerialName("cm")
    internal val name : String,
    @SerialName("dr")
    val durationFrames : Float
)
