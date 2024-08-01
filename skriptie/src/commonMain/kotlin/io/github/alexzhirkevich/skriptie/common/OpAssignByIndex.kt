package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JsArray
import io.github.alexzhirkevich.skriptie.javascript.numberOrNull

internal class OpAssignByIndex<C : ScriptRuntime>(
    private val variableName : String,
    private val scope : VariableType?,
    private val index : Expression<C>,
    private val assignableValue : Expression<C>,
    private val merge : ((Any?, Any?) -> Any?)?
) : Expression<C> {

    override tailrec fun invokeRaw(context: C): Any? {
        val v = assignableValue.invoke(context)
        val current = context.getVariable(variableName)

        check(merge == null || current != null) {
            "Cant modify $variableName as it is undefined"
        }

        if (current == null) {
            context.setVariable(variableName, mutableListOf<Any>(), scope)
            return invoke(context)
        } else {
            val i = index.invoke(context).numberOrNull()

            val index = checkNotNull(i as? Number) {
                "Unexpected index: $i"
            }.toInt()

            return when (current) {

                is JsArray-> {
                    while (current.lastIndex < index) {
                        current.add(Unit)
                    }

                    val c = current[index]

                    current[index] = if (current[index] !is Unit && merge != null){
                        merge.invoke(c,v)
                    } else {
                        v
                    }
                    current[index]
                }
                else -> error("Can't assign '$current' by index ($index)")
            }
        }
    }
}