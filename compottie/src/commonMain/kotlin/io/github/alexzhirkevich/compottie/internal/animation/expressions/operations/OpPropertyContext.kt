package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs

internal sealed class OpPropertyContext : Expression, ExpressionContext<RawProperty<*>> {

    final override fun interpret(op: String, args: List<Expression>): Expression {
        return when (op) {
            "value" -> OpPropertyValue()
            "numKeys" -> withContext { _, _, _ ->
                if (this is RawKeyframeProperty<*, *>) {
                    keyframes.size
                } else 0
            }
            "propertyIndex" -> withContext { _, _, _ -> index ?: Undefined }

            "valueAtTime" -> {
                checkArgs(args, 1, op)
                OpPropertyValue(timeRemapping = args[0])
            }

            "getVelocityAtTime",
            "getSpeedAtTime",
            "smooth",
            "loopIn",
            "loopOut" -> error("$op for Property is not yet supported")
            else -> unresolvedProperty(op, "Property")
        }
    }
}