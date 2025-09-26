package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.invoke
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.Transient

internal abstract class DynamicProperty<T : Any> : ExpressionProperty<T>() {

    @Transient
    var dynamic: PropertyProvider<T>? = null

    final override fun interpolated(state: AnimationState): T {
        return dynamic.derive(super.interpolated(state), state)
    }

    final override fun interpolatedFloat(state: AnimationState): Float {
        val d = dynamic ?: return super.interpolatedFloat(state)
        return d.invoke(state, super.interpolated(state)) as Float
    }

    final override fun interpolatedVec(state: AnimationState): Long {
        val d = dynamic ?: return super.interpolatedVec(state)
        return (d.invoke(state, super.interpolated(state)) as Vec2).packedValue
    }

    final override fun interpolatedColor(state: AnimationState): ULong {
        val d = dynamic ?: return super.interpolatedColor(state)
        return (d.invoke(state, super.interpolated(state)) as Color).value
    }
}