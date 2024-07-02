package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAssign
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpSub

internal class ExpressionEvaluator(
    expression : String
) {

    private val variables = mutableMapOf<String, Any>()

    private var disabled: Boolean = false


    private val operations by lazy {
        expression
            .replace("\t", "")
            .replace("\r", "")
            .replace("\\\"", "\"")
            .replace("var", "")
            .replace(" ", "")
            .split(";", "\n")
            .filter(String::isNotBlank)
            .mapNotNull {
                if (disabled) {
                    return@mapNotNull null
                }
                if ('=' !in it) {
                    Compottie.logger?.warn("Expression '$it' doesn't contain assignments. It was skipped")
                    return@mapNotNull null
                }
                try {
                    val name = it.substringBefore("=").trimEnd('+','-','*', '/')
                    val merge: ((Any, Any) -> Any)? = when {
                        "+=" in it -> OpAdd::invoke
                        "-=" in it -> OpSub::invoke
                        "*=" in it -> OpMul::invoke
                        "/=" in it -> OpDiv::invoke
                        ("==" !in it || it.indexOf("=") < it.indexOf("==")) -> null

                        else -> error("Invalid assignment")
                    }
                    val value = OperationParser(it.substringAfter("=")).parse()

                    OpAssign(name, value, merge)
                } catch (t: Throwable) {
                    Compottie.logger?.error(
                        "Unsupported or invalid Lottie expression: $it", t
                    )
                    disabled = true
                    null
                }
            }
    }

    fun evaluate(value: Any, state: AnimationState): Any {
        if (!state.expressionsEnabled || disabled) {
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