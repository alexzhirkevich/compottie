package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpBlock(
    val expressions: List<Expression>,
    var scoped : Boolean,
) : Expression {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return if (scoped){
            context.withScope(emptyMap()){
               invokeInternal(property, it, state)
            }
        } else {
            invokeInternal(property, context, state)
        }
    }

    private fun invokeInternal(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ) : Any {
        expressions.fastForEach {
            if (it is OpReturn) {
                return it(property, context, state)
            } else {
                it(property, context, state)
            }
        }
        return Undefined
    }
}