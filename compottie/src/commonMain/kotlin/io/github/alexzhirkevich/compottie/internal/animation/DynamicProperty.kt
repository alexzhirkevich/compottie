package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionInterpreter
import io.github.alexzhirkevich.compottie.internal.animation.expressions.MainExpressionInterpreter
import io.github.alexzhirkevich.compottie.internal.animation.expressions.RawExpressionEvaluator
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal abstract class DynamicProperty<T : Any> : ExpressionProperty<T>() {

    @Transient
    var dynamic: PropertyProvider<T>? = null

    override fun interpolated(state: AnimationState): T {
        return dynamic.derive(super.interpolated(state), state)
    }
}