package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.RawKeyframeProperty
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argForNameOrIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpSmooth
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpTemporalWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpLoopIn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpLoopOut
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpCreatePath
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpPropertyValue

internal abstract class OpPropertyContext : Expression, ExpressionContext<RawProperty<*>> {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): RawProperty<Any> = TODO()

    final override fun interpret(op: String?, args: List<Expression>): Expression? {
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
                points = args.argForNameOrIndex(0, "points"),
                inTangents = args.argForNameOrIndex(1, "inTangents"),
                outTangents = args.argForNameOrIndex(2, "outTangents"),
                isClosed = args.argForNameOrIndex(3, "is_closed"),
            )

            "propertyIndex" -> withContext { _, _, _ -> index ?: Undefined }

            "valueAtTime" -> {
                checkArgs(args, 1, op)
                OpPropertyValue(this, timeRemapping = args.argAt(0))
            }

            "wiggle" -> OpWiggle(
                property = this,
                freq = args.argForNameOrIndex(0, "freq")!!,
                amp = args.argForNameOrIndex(1, "amp")!!,
                octaves = args.argForNameOrIndex(2, "octaves"),
                ampMult = args.argForNameOrIndex(3, "amp_mult"),
                time = args.argForNameOrIndex(4, "t"),
            )

            "temporalWiggle" -> OpTemporalWiggle(
                freq = args.argForNameOrIndex(0, "freq")!!,
                amp = args.argForNameOrIndex(1, "amp")!!,
                octaves = args.argForNameOrIndex(2, "octaves"),
                ampMult = args.argForNameOrIndex(3, "amp_mult"),
                time = args.argForNameOrIndex(4, "t"),
            )

            "smooth" -> OpSmooth(
                prop = this,
                width = args.argForNameOrIndex(0, "width"),
                samples = args.argForNameOrIndex(1, "samples"),
                time = args.argForNameOrIndex(2, "t")
            )

            "loopIn", "loopInDuration" -> OpLoopIn(
                property = this,
                name = args.getOrNull(0),
                numKf = args.getOrNull(1),
                isDuration = op == "loopInDuration"
            )

            "loopOut", "loopOutDuration" -> OpLoopOut(
                property = this,
                name = args.argForNameOrIndex(0, "type"),
                numKf = args.argForNameOrIndex(1, "numKeyframes"),
                isDuration = op == "loopOutDuration"
            )

            "getVelocityAtTime",
            "getSpeedAtTime"
            -> error("$op is not yet supported")

            else -> null
        }
    }
}