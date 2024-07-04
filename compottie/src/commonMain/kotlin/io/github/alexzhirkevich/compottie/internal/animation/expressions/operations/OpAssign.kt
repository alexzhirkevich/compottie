package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EXPR_DEBUG_PRINT_ENABLED
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ValueExpressionInterpreter

internal class OpAssign(
    variableName : String,
    private val assignableValue : Expression,
    private val merge : ((Any, Any) -> Any)?
) : Expression {

    private val indexOp: Expression? = variableName
        .substringAfter("[", "")
        .substringBeforeLast("]", "")
        .takeIf(String::isNotBlank)
        ?.let {
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("Parsing assignment index: $it")
            }
            ValueExpressionInterpreter(it).interpret()
        }

    private val realVarName = variableName
        .trim()
        .removePrefix("var ")
        .removePrefix("let ")
        .removePrefix("const ")
        .substringBefore('[')

    override tailrec fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState,
    ): Undefined {
        val v = assignableValue.invoke(property, context, state)
        val current = context.variables[realVarName]

        check(merge == null || current != null) {
            "Cant modify $realVarName as it is undefined"
        }

        context.variables[realVarName] = if (indexOp == null) {
            if (current != null && merge != null) {
                merge.invoke(current, v)
            } else v
        } else {
            if (current == null) {
                context.variables[realVarName] = Vec2(0f, 0f)
                return invoke(property, context, state)
            } else {
                val i = indexOp.invoke(property, context, state)
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