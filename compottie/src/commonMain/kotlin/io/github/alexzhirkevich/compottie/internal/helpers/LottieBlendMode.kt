package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.graphics.BlendMode
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
internal value class LottieBlendMode(val type : Byte){
    companion object {
        val Normal = LottieBlendMode(0)
        val Multiply = LottieBlendMode(1)
        val Screen = LottieBlendMode(2)
        val Overlay = LottieBlendMode(3)
        val Darken = LottieBlendMode(4)
        val Lighten = LottieBlendMode(5)
        val ColorDodge = LottieBlendMode(6)
        val ColorBurn = LottieBlendMode(7)
        val HardLight = LottieBlendMode(8)
        val SoftLight = LottieBlendMode(9)
        val Difference = LottieBlendMode(10)
        val Exclusion = LottieBlendMode(11)
        val Hue = LottieBlendMode(12)
        val Saturation = LottieBlendMode(13)
        val Color = LottieBlendMode(14)
        val Luminosity = LottieBlendMode(15)
    }
}

internal fun LottieBlendMode.asComposeBlendMode() : BlendMode {
    return BlendModeMapping[this] ?: error("Unknown lottie blend mode: $this")
}

private val BlendModeMapping by lazy {
    mapOf(
        LottieBlendMode.Normal to null,
        LottieBlendMode.Multiply to BlendMode.Multiply,
        LottieBlendMode.Screen to BlendMode.Screen,
        LottieBlendMode.Overlay to BlendMode.Overlay,
        LottieBlendMode.Darken to BlendMode.Darken,
        LottieBlendMode.Lighten to BlendMode.Lighten,
        LottieBlendMode.ColorDodge to BlendMode.ColorDodge,
        LottieBlendMode.ColorBurn to BlendMode.ColorBurn,
        LottieBlendMode.HardLight to BlendMode.Hardlight,
        LottieBlendMode.SoftLight to BlendMode.Softlight,
        LottieBlendMode.Difference to BlendMode.Difference,
        LottieBlendMode.Exclusion to BlendMode.Exclusion,
        LottieBlendMode.Hue to BlendMode.Hue,
        LottieBlendMode.Saturation to BlendMode.Saturation,
        LottieBlendMode.Color to BlendMode.Color,
        LottieBlendMode.Luminosity to BlendMode.Luminosity
    )
}