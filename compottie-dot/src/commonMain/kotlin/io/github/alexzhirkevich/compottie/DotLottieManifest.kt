package io.github.alexzhirkevich.compottie

import kotlinx.serialization.Serializable

@Serializable
internal class DotLottieManifest(
    val animations : List<DotLottieAnimation>
)

@Serializable
internal class DotLottieAnimation(
    val id : String? = null,
    val speed : Float = 1f,
    val loop : Boolean = false
)