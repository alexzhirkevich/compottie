package io.github.alexzhirkevich.compottie.dot

import kotlinx.serialization.Serializable

@Serializable
internal class DotLottieManifest(
    val animations : List<DotLottieAnimation>,
    val themes : List<DotLottieTheme>? = null
)

@Serializable
internal class DotLottieAnimation(
    val id : String? = null,
    val speed : Float = 1f,
    val loop : Boolean = false
)
@Serializable
internal class DotLottieTheme(
    val id : String,
    val name : String? = null
)

