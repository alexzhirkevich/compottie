package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.exp

internal class ExpressionEvaluator(
    expression : String
) {

    private val variables = mutableMapOf<String, Any>()

    private val operations by lazy {
        expression
            .replace("\\n", "")
            .replace("\n", "")
            .replace("\t", "")
            .replace("\r", "")
            .replace("var", "")
            .replace(" ", "")
            .split(";")
            .filter(String::isNotBlank)
            .mapNotNull {
                if ('=' !in it) {
                    return@mapNotNull null
                }
                var variable = it.substringBefore("=")
                val index = variableIdx(variable)
                variable = variable.substringBefore("[")

                val expr = it.substringAfter("=")

                if (expr == "[]")
                    return@mapNotNull null

                val op = OperationParser(expr).parse()

                Operation { v, vars, s ->
                    setVariable(variable, index, op(v, vars, s))
                }
            }
    }

    fun evaluate(value: Any, state: AnimationState): Any {
        if (!state.expressionsEnabled) {
            return value
        }
        variables.clear()
        operations.fastForEach {
            it(value, variables, state)
        }
        return checkNotNull(variables["\$bm_rt"]) {
            "\$bm_rt is null"
        }
    }

    private fun setVariable(variable: String, index: Int?, value: Any) {
//        println("Setting $variable[$index] to $value")
        if (index == null) {
            variables[variable] = value
        } else {
            when (val v = variables[variable]) {
                is Vec2 -> {
                    variables[variable] = when (index) {
                        0 -> v.copy(x = value as Float)
                        1 -> v.copy(y = value as Float)
                        else -> v
                    }
                }

                null -> {
                    variables[variable] = Vec2(0f, 0f)
                    setVariable(variable, index, value)
                }
            }
        }
    }
}