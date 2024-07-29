package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EXPR_DEBUG_PRINT_ENABLED
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argForNameOrIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgsNotNull
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.color.OpHslToRgb
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.color.OpRgbToHsl
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpGetComp
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpGetLayer
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.composition.OpGetProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpIfCondition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpClamp
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpDegreesToRadians
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMath
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMod
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpRadiansToDegree
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpSub
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpNoise
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpRandomNumber
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpSetRandomSeed
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpFramesToTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpGetTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpInterpolate
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.OpTimeToFrames
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpGetVariable
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpVar
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec.OpDot
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec.OpLength
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.vec.OpNormalize

internal object OpGlobalContext : ExpressionContext<Undefined> {

    // global context extends thisProperty context
    private val thisProperty = OpGetProperty()
    private val thisLayer = OpGetLayer()
    private val thisComp = OpGetComp(null)

    override fun interpret(callable: String?, args: List<Expression>?): Expression? {
        return when (callable) {
            "var", "let", "const" -> {
                OpVar(
                    when (callable) {
                        "var" -> VariableType.Var
                        "let" -> VariableType.Let
                        else -> VariableType.Const
                    }
                )
            }
            "Infinity" -> {
                OpConstant(Float.POSITIVE_INFINITY)
            }
            "Math" -> {
                OpMath
            }
            "time" -> {
                OpGetTime
            }
            "thisComp" -> {
                if (args.isNullOrEmpty()) {
                    OpGetComp(null)
                } else {
                    OpGetLayer(nameOrIndex = args.single())
                }
            }

            "comp" -> {
                checkArgs(args, 1, callable)
                return OpGetComp(args.argAt(0))
            }

            "thisLayer" -> {
                OpGetLayer()
            }
            "thisProperty" -> {
                thisProperty
            }
            "add", "\$bm_sum", "sum" -> {
                checkArgs(args, 2, callable)
                OpAdd(
                    args.argForNameOrIndex(0, "vec1")!!,
                    args.argForNameOrIndex(1, "vec2")!!,
                )
            }

            "sub", "\$bm_sub" -> {
                checkArgs(args, 2, callable)
                OpSub(
                    args.argForNameOrIndex(0, "vec1")!!,
                    args.argForNameOrIndex(1, "vec2")!!,
                )
            }

            "mul", "\$bm_mul" -> {
                checkArgs(args, 2, callable)
                OpMul(
                    args.argForNameOrIndex(0, "vec")!!,
                    args.argForNameOrIndex(1, "amount")!!,
                )
            }

            "div", "\$bm_div" -> {
                checkArgs(args, 2, callable)
                OpDiv(
                    args.argForNameOrIndex(0, "vec")!!,
                    args.argForNameOrIndex(1, "amount")!!,
                )
            }

            "mod" -> {
                checkArgs(args, 2, callable)
                OpMod(
                    args.argForNameOrIndex(0, "vec")!!,
                    args.argForNameOrIndex(1, "amount")!!,
                )
            }

            "clamp" -> {
                checkArgs(args, 3, callable)
                OpClamp(
                    args.argForNameOrIndex(0, "value")!!,
                    args.argForNameOrIndex(1, "limit1")!!,
                    args.argForNameOrIndex(2, "limit2")!!,
                )
            }

            "dot" -> {
                checkArgs(args, 2, callable)
                OpDot(
                    args.argForNameOrIndex(0, "vec1")!!,
                    args.argForNameOrIndex(1, "vec2")!!,
                )
            }

            "length" -> {
                checkArgsNotNull(args, callable)
                OpLength(
                    args.argForNameOrIndex(0, "vec", "point1")!!,
                    args.argForNameOrIndex(1, "point2"),
                )
            }
            "normalize" -> {
                checkArgs(args, 1, callable)
                OpNormalize(args.argAt(0))
            }

            "cross" -> {
                error("cross is not supported yet") //todo: support OpCross
            }

            "degreesToRadians" -> {
                checkArgs(args, 1, callable)
                OpDegreesToRadians(args.argAt(0))
            }

            "radiansToDegrees" -> {
                checkArgs(args, 1, callable)
                OpRadiansToDegree(args.argAt(0))
            }

            "timeToFrames" -> {
                checkArgsNotNull(args, callable)
                OpTimeToFrames(
                    args.argForNameOrIndex(0,"t"),
                    args.argForNameOrIndex(1,"fps"),
                )
            }
            "framesToTime" -> {
                checkArgsNotNull(args, callable)
                OpFramesToTime(
                    args.argForNameOrIndex(0,"frames"),
                    args.argForNameOrIndex(1,"fps"),
                )
            }
            "seedRandom" -> {
                checkArgsNotNull(args, callable)
                OpSetRandomSeed(
                    args.argForNameOrIndex(0,"offset")!!,
                    args.argForNameOrIndex(1,"timeless"),
                )
            }
            "random", "gaussRandom" -> {
                checkArgsNotNull(args, callable)
                OpRandomNumber(
                    args.argForNameOrIndex(0,"maxValOrArray1"),
                    args.argForNameOrIndex(1,"maxValOrArray2"),
                    isGauss = callable == "gaussRandom"
                )
            }

            "noise" -> {
                checkArgs(args, 1, callable)
                OpNoise(args.argAt(0))
            }

            "linear" -> {
                checkArgsNotNull(args, callable)
                OpInterpolate.interpret(LinearEasing, args)
            }
            "ease" -> {
                checkArgsNotNull(args, callable)
                OpInterpolate.interpret(easeInOut, args)
            }
            "easeIn" -> {
                checkArgsNotNull(args, callable)
                OpInterpolate.interpret(easeIn, args)
            }
            "easeOut" -> {
                checkArgsNotNull(args, callable)
                OpInterpolate.interpret(easeOut, args)
            }

            "hslToRgb" -> {
                checkArgs(args, 1, callable)
                OpHslToRgb(args.argAt(0))
            }

            "rgbToHsl" -> {
                checkArgs(args, 1, callable)
                OpRgbToHsl(args.argAt(0))
            }

            "if" -> {
                checkArgsNotNull(args, callable)
                OpIfCondition(condition = args.single())
            }//error("Compottie doesn't support conditions in expressions yet")
            "true" -> {
                OpConstant(true)
            }
            "false" -> {
                OpConstant(false)
            }
            else -> {
                thisProperty.interpret(callable, args)
                    ?: thisLayer.interpret(callable, args)
                    ?: thisComp.interpret(callable, args)
                    ?: run {
                        if (args == null && callable != null) {
                            if (EXPR_DEBUG_PRINT_ENABLED) {
                                println("making GetVariable $callable...")
                            }
                            OpGetVariable(callable)
                        } else {
                            null
                        }
                    }
            }
        }
    }

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any = Undefined
}

internal val easeInOut = CubicBezierEasing(0.33f, 0f, 0.667f, 1f)
internal val easeOut = CubicBezierEasing(0.167f, 0.167f, 0.667f, 1f)
internal val easeIn = CubicBezierEasing(0.333f, 0f, 0.833f, 0.833f)