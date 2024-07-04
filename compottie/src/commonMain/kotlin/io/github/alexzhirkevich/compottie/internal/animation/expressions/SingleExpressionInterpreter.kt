package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAssign
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpSub

internal class SingleExpressionInterpreter(
    private val expr : String,
) : ExpressionInterpreter {

    override fun interpret(): Expression {
        return if ('=' !in expr || "==" in expr && expr.indexOf("=") >= expr.indexOf("==")) {
            ValueExpressionInterpreter(expr).interpret()
        } else {
            return OpAssign(
                variableName = expr
                    .substringBefore("=")
                    .trimEnd('+', '-', '*', '/'),
                assignableValue = ValueExpressionInterpreter(expr.substringAfter("=")).interpret(),
                merge = when {
                    "+=" in expr -> OpAdd.Companion::invoke
                    "-=" in expr -> OpSub.Companion::invoke
                    "*=" in expr -> OpMul.Companion::invoke
                    "/=" in expr -> OpDiv.Companion::invoke
                    else -> null
                }
            )
        }
    }
}

