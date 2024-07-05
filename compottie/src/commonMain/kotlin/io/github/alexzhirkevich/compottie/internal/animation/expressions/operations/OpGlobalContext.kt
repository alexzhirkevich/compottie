package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EXPR_DEBUG_PRINT_ENABLED
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpGetComp
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpGetLayer
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpGetProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.condition.OpIfCondition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpClamp
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec.OpDot
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMath
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMod
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpSub
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpNoise
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpRandomNumber
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpSetRandomSeed
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpWiggle
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpFramesToTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpInterpolate
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpTimeToFrames
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpGetVariable
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpPropertyValue
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpVar
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec.OpLength
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec.OpNormalize

internal object OpGlobalContext : ExpressionContext<Nothing>, Expression {

    override fun interpret(op: String, args: List<Expression>): Expression {
        return when (op) {
            "var", "let", "const" -> OpVar
            "Math" -> OpMath
            "time" -> OpGetTime
            "value" -> OpPropertyValue()
            "thisComp" -> {
                if (args.isEmpty()) {
                    OpGetComp(null)
                } else {
                    OpGetLayer(name = args.single())
                }
            }

            "comp" -> {
                checkArgs(args, 1, op)
                return OpGetComp(args[0])
            }

            "thisLayer" -> OpGetLayer()
            "thisProperty" -> OpGetProperty()
            "add", "\$bm_sum", "sum" -> {
                checkArgs(args, 2, op)
                OpAdd(args[0], args[1])
            }

            "dot" -> {
                checkArgs(args, 2, op)
                OpDot(args[0], args[1])
            }

            "length" -> OpLength(args[0], args.getOrNull(1))
            "normalize" -> {
                checkArgs(args, 1, op)
                OpNormalize(args[0])
            }

            "cross" -> error("cross is not supported yet") //todo: support OpCross
            "sub", "\$bm_sub" -> {
                checkArgs(args, 2, op)
                OpSub(args[0], args[1])
            }

            "mul", "\$bm_mul" -> {
                checkArgs(args, 2, op)
                OpMul(args[0], args[1])
            }

            "div", "\$bm_div" -> {
                checkArgs(args, 2, op)
                OpDiv(args[0], args[1])
            }

            "mod" -> {
                checkArgs(args, 2, op)
                OpMod(args[0], args[1])
            }

            "clamp" -> {
                checkArgs(args, 3, op)
                OpClamp(args[0], args[1], args[2])
            }

            "timeToFrames" -> OpTimeToFrames(args.getOrNull(0), args.getOrNull(1))
            "framesToTime" -> OpFramesToTime(args.getOrNull(0), args.getOrNull(1))
            "seedRandom" -> OpSetRandomSeed(args[0], args.getOrNull(1))
            "random", "gaussRandom" -> {
                OpRandomNumber(
                    args.getOrNull(0),
                    args.getOrNull(1),
                    isGauss = op == "gaussRandom"
                )
            }
            "noise" -> {
                checkArgs(args,1, op)
                OpNoise(args[0])
            }
            "linear" -> OpInterpolate.parse(LinearEasing, args)
            "ease" -> OpInterpolate.parse(EaseInOut, args)
            "easeIn" -> OpInterpolate.parse(EaseIn, args)
            "easeOut" -> OpInterpolate.parse(EaseOut, args)

            "wiggle" -> OpWiggle(args[0], args[1], args.getOrNull(2), args.getOrNull(3))

            "if" -> {
                OpIfCondition(condition = args.single())
            }//error("Compottie doesn't support conditions in expressions yet")
            "true" -> OpConstant(true)
            "false" -> OpConstant(false)
            else -> {
                require(args.isEmpty()) {
                    "Unknown function: $op"
                }
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("made variable $op")
                }
                OpGetVariable(op)
            }
        }
    }

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return Undefined
    }
}