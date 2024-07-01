package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

internal class ExpressionEvaluator(
    expression : String
) {
    private val statements = expression
        .replace(" ", "")
        .replace("\\n","")
        .replace("\n","")
        .replace("\t","")
        .replace("var", "")
        .split(";")

    private var variables = mutableMapOf<String, Any>()

    inline fun <reified T> evaluate(value : T, state: AnimationState) : T {
        variables.clear()
        statements.fastForEach {
            evaluate(it, value, state)
        }
        return variables["\$bm_rt"] as T
    }

    private fun <T> evaluate(statement : String, value : T, state: AnimationState) {
        val variable = statement.substringBefore("=")
        val expr = statement.substringAfter("=")
    }

    private fun setValue(variable : String, value : Any) {

    }

    fun add(a : Any, b : Any) : Any {
        return when {
            a is Number && b is Number ->  a.toFloat() + b.toFloat()
            a is Vec2 && b is Vec2 -> a+b
            else -> exprErr("can't sum $a and $b")
        }
    }

    private fun exprErr(msg : String) : Nothing = error("Expression error: $msg")
}
