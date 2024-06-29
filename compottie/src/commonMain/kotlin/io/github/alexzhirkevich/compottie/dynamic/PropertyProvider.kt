package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ScaleFactor
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

typealias PropertyProvider<T> = AnimationState.(source : T) -> T

internal fun <F, T> PropertyProvider<F>.map(
    from : (F) -> T,
    to : (T) -> F
) : PropertyProvider<T> = {
    from(derive(to(it), this))
}

/**
 * Returns [source] if this is null and provided value otherwise
 * */
internal fun <T> PropertyProvider<T>?.derive(source : T, state: AnimationState) : T {
    return if (this == null) source else invoke(state, source)
}

internal fun Vec2.toScaleFactor() = ScaleFactor(x, y)
internal fun Vec2.toSize() = Size(x, y)
internal fun Size.toVec2() = Vec2(width, height)
internal fun ScaleFactor.toVec2() = Vec2(scaleX * 100f, scaleY * 100f)
internal fun Offset.toVec2() = this

internal fun Vec2.toOffset() = this

private val IdentityScaleFactor = ScaleFactor(1f,1f)

internal val ScaleFactor.Companion.Identity get() = IdentityScaleFactor