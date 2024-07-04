package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpGetProperty(
    private val property : Expression? = null
) : OpPropertyContext() {

    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): PropertyAnimation<Any> {
        return if (this.property != null) {
            this.property.invoke(property, variables, state) as PropertyAnimation<*>
        } else property
    }
}