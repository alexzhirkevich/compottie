package io.github.alexzhirkevich.compottie.internal.effects

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.layers.BaseLayer
import io.github.alexzhirkevich.compottie.internal.platform.effects.PlatformDropShadowEffect
import io.github.alexzhirkevich.compottie.internal.platform.effects.applyNativeDropShadowEffect
import io.github.alexzhirkevich.compottie.internal.platform.effects.makeNativeDropShadowEffect
import io.github.alexzhirkevich.compottie.internal.platform.effects.resetEffects
import io.github.alexzhirkevich.compottie.internal.platform.setBlurMaskFilter
import io.github.alexzhirkevich.compottie.internal.utils.Math
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal class LayerEffectsApplier(
    private val layer: BaseLayer
) {
    fun applyTo(paint: Paint, animationState: AnimationState, effectState : LayerEffectsState) {

        layer.effects.fastForEachReversed {
            when (it){
                is BlurEffect -> paint.applyBlurEffect(it, animationState, effectState)
                is FillEffect -> paint.applyFillEffect(it, animationState, effectState)
                is TintEffect -> paint.applyTintEffect(it, animationState, effectState)
                is DropShadowEffect -> {
//                    paint.applyDropShadowEffect(it, animationState, effectState)
                }
                LayerEffect.UnsupportedEffect -> {

                }
            }
        }

        effectState.lastPaint = paint
    }
}

internal class LayerEffectsState {
    var blurRadius: Float? = null
    var lastPaint : Paint? = null

    var lastFillColor : Color? = null
    var lastFillFilter : ColorFilter? = null

    var dropShadowHash : Int? = null
    var dropShadowEffect : PlatformDropShadowEffect? = null

    var tintHash : Int? = null
    var tintColorFiter : ColorFilter? = null
}

private fun Paint.applyBlurEffect(
    effect: BlurEffect,
    animationState: AnimationState,
    effectState: LayerEffectsState
) {
    val radius = effect.radius?.interpolated(animationState)?.takeIf { it > 0f } ?: return

    if (this !== effectState.lastPaint || radius != effectState.blurRadius) {
        setBlurMaskFilter(radius)
        effectState.blurRadius = radius
    }
}

private fun Paint.applyFillEffect(
    effect: FillEffect,
    animationState: AnimationState,
    effectState: LayerEffectsState
) {
    val color = effect.color?.interpolated(animationState)?.let {
        it.copy(                              // don't divide by 100
            alpha = it.alpha * (effect.opacity?.interpolated(animationState)?.coerceIn(0f, 1f) ?: 1f)
        )
    }
    if (this !== effectState.lastPaint || effectState.lastFillColor != color) {

        colorFilter = color?.let {
            ColorFilter.tint(color)
        }
        effectState.lastFillFilter = colorFilter
        effectState.lastFillColor = color
    } else {
        colorFilter = effectState.lastFillFilter
    }
}

private fun Paint.applyTintEffect(
    effect: TintEffect,
    animationState: AnimationState,
    effectState: LayerEffectsState
) {
    val intensity = effect.intensity?.interpolatedNorm(animationState)
        ?.coerceIn(0f, 1f) ?: 1f

    val black = effect.black?.interpolated(animationState)?.let {
        it.copy(alpha = it.alpha * intensity)
    } ?: Color.Black
    val white = effect.white?.interpolated(animationState)?.let {
        it.copy(alpha = it.alpha * intensity)
    }

    if (black.red != 0f || black.green != 0f || black.blue != 0f)
        return //unsupported

    val hash = white.hashCode()

    if (this === effectState.lastPaint &&
        hash == effectState.tintHash &&
        effectState.tintColorFiter != null
    ){
        colorFilter = effectState.tintColorFiter
        return
    }
    colorFilter = if (white != null) {
        ColorFilter.tint(white, BlendMode.Modulate)
    } else null

    effectState.tintHash = hash
    effectState.tintColorFiter = colorFilter
}

internal fun Paint.applyDropShadowEffect(
    effect: DropShadowEffect,
    animationState: AnimationState,
    effectState: LayerEffectsState,
) {

    val directionRad = Math.toRadians(effect.angle?.interpolated(animationState) ?: 0f)

    val distance = effect.distance?.interpolated(animationState) ?: 0f
    val x = (sin(directionRad)) * distance
    val y = (cos(directionRad + PI).toFloat()) * distance

    val baseColor = effect.color?.interpolated(animationState) ?: Color.Black

    val opacity = effect.opacity?.interpolated(animationState)?.div(100)?.coerceIn(0f, 1f) ?: 1f

    val color = baseColor.copy(
        opacity * baseColor.alpha,
        baseColor.red,
        baseColor.green,
        baseColor.blue
    )
    val radius: Float = effect.blur?.interpolated(animationState) ?: 0f

    val hash = arrayOf(x, y, color, radius).contentHashCode()

    if (effectState.lastPaint !== this || hash != effectState.dropShadowHash || effectState.dropShadowEffect == null) {
        effectState.dropShadowEffect = makeNativeDropShadowEffect(radius, x, y, color)
        effectState.dropShadowHash = hash
    }

    applyNativeDropShadowEffect(effectState.dropShadowEffect!!)
}