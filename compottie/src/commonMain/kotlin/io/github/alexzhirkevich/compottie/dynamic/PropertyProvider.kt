package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ScaleFactor
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

typealias PropertyProvider<T> = AnimationState.(source : T) -> T

/**
 * Returns [source] if this is null and provided value otherwise
 * */
internal fun <T> PropertyProvider<T>?.derive(source : T, state: AnimationState) : T {
    return if (this == null) source else invoke(state, source)
}

internal fun ScaleFactor.toVec2() = Vec2(scaleX,scaleY)
internal fun Vec2.toScaleFactor() = ScaleFactor(x, y)

private val IdentityScaleFactor = ScaleFactor(1f,1f)

internal val ScaleFactor.Companion.Identity get() = IdentityScaleFactor