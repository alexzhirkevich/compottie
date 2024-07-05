package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpAssignByIndex(
    private val variableName : String,
    private val index : Expression,
    private val assignableValue : Expression,
    private val merge : ((Any, Any) -> Any)?
) : Expression {


    override tailrec fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Undefined {
        val v = assignableValue.invoke(property, context, state)
        val current = context.variables[variableName]

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        context.variables[variableName] = if (current == null) {
            context.variables[variableName] = Vec2(0f, 0f)
            return invoke(property, context, state)
        } else {
            val i = index.invoke(property, context, state)
            val index = checkNotNull(i as? Number) {
                "Unexpected index: $i"
            }.toInt()

            when (current) {
                is Vec2 -> {
                    check(v is Number) {
                        "Cant assign $v to $index "
                    }
                    when (index) {
                        0 -> current.copy(
                            x = if (merge == null)
                                v.toFloat()
                            else merge.invoke(current.x, v.toFloat()) as Float
                        )

                        1 -> current.copy(
                            y = if (merge == null)
                                v.toFloat()
                            else merge.invoke(current.y, v.toFloat()) as Float
                        )

                        else -> error("Cant get $index index. Array length is 2")
                    }
                }

                else -> error("Can't assign '$current' index to $index")
            }
        }


        return Undefined
    }
}