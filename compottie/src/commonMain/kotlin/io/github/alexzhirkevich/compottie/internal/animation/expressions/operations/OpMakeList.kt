package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal class OpMakeList(
    private val items : List<Expression>
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Any {
        val args = items.fastMap { it.invoke(property, context, state) }

        if (args.isEmpty()) {
            return Vec2(0f, 0f)
        }
        if ((args.size == 2 || args.size == 3) && args.all { it is Number }) {
            return Vec2((args[0] as Number).toFloat(), (args[1] as Number).toFloat())
        }

        error("Can't make a list of $args")
    }
}