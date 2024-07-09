package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.getForNameOrIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpSmooth
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpTemporalWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpLoopIn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpLoopOut
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpCreatePath
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpPropertyValue

internal abstract class OpPropertyContext : Expression, ExpressionContext<RawProperty<*>> {

    final override fun interpret(op: String, args: List<Expression>): Expression? {
        return when (op) {
            "value" -> OpPropertyValue(this)
            "numKeys" -> withContext { _, _, _ ->
                (this as? RawKeyframeProperty<*, *>)?.keyframes?.size ?: 0
            }

            "points" -> withTimeRemapping(args.getOrNull(0)) { _, _, state ->
                (this as? AnimatedShape)?.rawBezier(state)?.vertices ?: Undefined
            }

            "inTangents" -> withTimeRemapping(args.getOrNull(0)) { _, _, state ->
                (this as? AnimatedShape)?.rawBezier(state)?.inTangents ?: Undefined
            }

            "outTangents" -> withTimeRemapping(args.getOrNull(0)) { _, _, state ->
                (this as? AnimatedShape)?.rawBezier(state)?.outTangents ?: Undefined
            }

            "isClosed" -> withTimeRemapping(args.getOrNull(0)) { _, _, state ->
                (this as? AnimatedShape)?.rawBezier(state)?.isClosed ?: Undefined
            }

            "createPath" -> OpCreatePath(
                points = args.getForNameOrIndex(0, "points"),
                inTangents = args.getForNameOrIndex(1, "inTangents"),
                outTangents = args.getForNameOrIndex(2, "outTangents"),
                isClosed = args.getForNameOrIndex(3, "is_closed"),
            )

            "propertyIndex" -> withContext { _, _, _ -> index ?: Undefined }

            "valueAtTime" -> {
                checkArgs(args, 1, op)
                OpPropertyValue(this, timeRemapping = args[0])
            }

            "wiggle" -> OpWiggle(
                property = this,
                freq = args.getForNameOrIndex(0, "freq")!!,
                amp = args.getForNameOrIndex(1, "amp")!!,
                octaves = args.getForNameOrIndex(2, "octaves"),
                ampMult = args.getForNameOrIndex(3, "amp_mult"),
                time = args.getForNameOrIndex(4, "t"),
            )

            "temporalWiggle" -> OpTemporalWiggle(
                freq = args.getForNameOrIndex(0, "freq")!!,
                amp = args.getForNameOrIndex(1, "amp")!!,
                octaves = args.getForNameOrIndex(2, "octaves"),
                ampMult = args.getForNameOrIndex(3, "amp_mult"),
                time = args.getForNameOrIndex(4, "t"),
            )

            "smooth" -> OpSmooth(
                prop = this,
                width = args.getForNameOrIndex(0, "width"),
                samples = args.getForNameOrIndex(1, "samples"),
                time = args.getForNameOrIndex(2, "t")
            )

            "loopIn", "loopInDuration" -> OpLoopIn(
                property = this,
                name = args.getOrNull(0),
                numKf = args.getOrNull(1),
                isDuration = op == "loopInDuration"
            )

            "loopOut", "loopOutDuration" -> OpLoopOut(
                property = this,
                name = args.getForNameOrIndex(0, "type"),
                numKf = args.getForNameOrIndex(1, "numKeyframes"),
                isDuration = op == "loopOutDuration"
            )

            "getVelocityAtTime",
            "getSpeedAtTime"
            -> error("$op is not yet supported")

            else -> null
        }
    }
}