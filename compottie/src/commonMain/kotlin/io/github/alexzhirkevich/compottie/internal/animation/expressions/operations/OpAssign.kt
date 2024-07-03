package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionParser
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined

internal class OpAssign(
    private val variableName : String,
    private val assignableValue : Expression,
    private val merge : ((Any, Any) -> Any)?
) : Expression {

    private val indexOp: Expression? = variableName
        .substringAfter("[", "")
        .substringBeforeLast("]", "")
        .takeIf(String::isNotBlank)
        ?.let {
            println("Parsing assignment index: $it")
            ExpressionParser(it, true).parse()
        }

    private val realVarName = variableName.substringBefore('[')


    override tailrec fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState,
    ): Any {
        val v = assignableValue.invoke(property, variables, state)
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
                return invoke(property, variables, state)
            } else {
                val i = indexOp.invoke(property, variables, state)
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