package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation

internal class ExpressionEvaluator(
    expression : String
) {

    private val variables = mutableMapOf<String, Any>()

    private var hasErrors: Boolean = false

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
                if (hasErrors) {
                    return@mapNotNull null
                }
                try {
                    ExpressionParser(it).parse()
                } catch (t: Throwable) {
                    Compottie.logger?.error(
                        "Unsupported or invalid Lottie expression: $it", t
                    )
                    hasErrors = true
                    null
                }
            }
    }

    fun evaluate(property: PropertyAnimation<Any>, state: AnimationState): Any {
        return try {
            if (!state.enableExpressions || hasErrors) {
                return property.rawInterpolated(state)
            }
            variables.clear()
            operations.fastForEach {
                it(property, variables, state)
            }
            checkNotNull(variables["\$bm_rt"]) {
                "\$bm_rt is null"
            }
        } catch (t: Throwable) {
            throw ExpressionException(
                "Error occurred in Lottie expression. Try disable expressions for Painter using enableExpressions=false",
                t
            )
        }
    }
}