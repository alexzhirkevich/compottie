package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OperationParser
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpAssign(
    private val variableName : String,
    private val value : Operation,
    private val merge : ((Any, Any) -> Any)?
) : Operation {

    private val indexOp: Operation? = variableName
        .substringAfter("[", "")
        .substringBeforeLast("]", "")
        .takeIf(String::isNotBlank)
        ?.let {
            println("Parsing assignment index: $it")
            OperationParser(it).parse()
        }

    private val realVarName = variableName.substringBefore('[')


    override tailrec fun invoke(
        value: Any,
        variables: MutableMap<String, Any>,
        state: AnimationState,
    ): Any {
        val v = value(value, variables, state)
        val current = variables[realVarName]

        check(merge == null || current != null) {
            "Cant modify $realVarName as it is undefined"
        }

        variables[realVarName] = if (indexOp == null) {
            if (current != null && merge != null) {
                merge.invoke(current, v)
            } else v
        } else {
            if (current == null) {
                variables[realVarName] = Vec2(0f, 0f)
                return invoke(value, variables, state)
            } else {
                val i = indexOp.invoke(value, variables, state)
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
        }

        return Undefined
    }
}