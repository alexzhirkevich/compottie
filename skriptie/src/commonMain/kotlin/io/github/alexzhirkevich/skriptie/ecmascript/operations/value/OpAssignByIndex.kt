package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType

internal class OpAssignByIndex<C : ScriptContext>(
    private val variableName : String,
    private val scope : VariableType?,
    private val index : Expression<C>,
    private val assignableValue : Expression<C>,
    private val merge : ((Any, Any) -> Any)?
) : Expression<C> {

    override tailrec fun invoke(context: C): Undefined {
        val v = assignableValue.invoke(context)
        val current = context.getVariable(variableName)

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        if (current == null) {
            context.setVariable(variableName, mutableListOf<Any>(), scope)
            return invoke(context)
        } else {
            val i = index.invoke(context)

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
                    return invoke(context)
                }

                else -> error("Can't assign '$current' by index ($index)")
            }
        }

        return Undefined
    }
}