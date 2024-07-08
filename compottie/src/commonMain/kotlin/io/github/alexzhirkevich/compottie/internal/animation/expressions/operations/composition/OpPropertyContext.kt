package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpLoopIn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpLoopOut
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpPropertyValue

internal abstract class OpPropertyContext : Expression, ExpressionContext<RawProperty<*>> {

    final override fun interpret(op: String, args: List<Expression>): Expression? {
        return when (op) {
            "value" -> OpPropertyValue(this)
            "numKeys" -> withContext { _, _, _ ->
                if (this is RawKeyframeProperty<*, *>) {
                    keyframes.size
                } else 0
            }

            "propertyIndex" -> withContext { _, _, _ -> index ?: Undefined }

            "valueAtTime" -> {
                checkArgs(args, 1, op)
                OpPropertyValue(this, timeRemapping = args[0])
            }

            "wiggle" -> OpWiggle(
                property = this,
                freq = args[0],
                amp = args[1],
                octaves = args.getOrNull(2),
                ampMult = args.getOrNull(3)
            )

            "loopIn", "loopInDuration" -> OpLoopIn(
                property = this,
                name = args.getOrNull(0),
                numKf = args.getOrNull(1),
                isDuration = op == "loopInDuration"
            )

            "loopOut", "loopOutDuration" -> OpLoopOut(
                property = this,
                name = args.getOrNull(0),
                numKf = args.getOrNull(1),
                isDuration = op == "loopOutDuration"
            )

            "getVelocityAtTime",
            "getSpeedAtTime",
            "smooth" -> error("$op is not yet supported")

            else -> null
        }
    }
}