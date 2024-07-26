package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.Transient

internal abstract class DynamicProperty<T : Any> : ExpressionProperty<T>() {

    @Transient
    var dynamic: PropertyProvider<T>? = null

    override fun interpolated(state: AnimationState): T {
        return dynamic.derive(super.interpolated(state), state)
    }
}