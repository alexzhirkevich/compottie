package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableScope

internal class OpAssignByIndex(
    private val variableName : String,
    private val scope : VariableScope,
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
        val current = context.getVariable(variableName)


        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        if (current == null) {
            context.setVariable(variableName, mutableListOf<Any>(), scope)
            return invoke(property, context, state)
        } else {
            val i = index.invoke(property, context, state)

            val index = checkNotNull(i as? Number) {
                "Unexpected index: $i"
            }.toInt()

            when (current) {

                is MutableList<*> -> {
                    current as MutableList<Any>

                    while (current.lastIndex < index) {
                        current.add(Undefined)
                    }

                    val c = current[index]

                    current[index] = if (current[index] !is Undefined && merge != null){
                        merge.invoke(c,v)
                    } else {
                        v
                    }
                }

                is List<*> -> {
                    context.setVariable(variableName, current.toMutableList(), scope)
                    return invoke(property, context, state)
                }

                else -> error("Can't assign '$current' by index ($index)")
            }
        }

        return Undefined
    }
}