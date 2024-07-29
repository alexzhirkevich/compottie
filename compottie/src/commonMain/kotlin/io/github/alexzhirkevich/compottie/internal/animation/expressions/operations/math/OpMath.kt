package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OpUndefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpRandomNumber
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.floor
import kotlin.math.ln1p
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.math.truncate

internal object OpMath : Expression, ExpressionContext<OpMath> {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return OpMath
    }

    override fun interpret(
        callable: String?,
        args: List<Expression>?
    ): Expression {
       return if (args == null){
           interpretVar(callable)
       } else {
           interpretFun(callable, args)
       }
    }

    private fun interpretVar(
        op: String?,
    ): Expression {
        return when (op) {
            "PI" -> PI
            "E" -> E
            "LN10" -> LN10
            "LN2" -> LN2
            "LOG10E" -> LOG10E
            "LOG2E" -> LOG2E
            "SQRT1_2" -> SQRT1_2
            "SQRT2" -> SQRT2
            else -> OpUndefined
        }
    }
    private fun interpretFun(
        op: String?,
        args: List<Expression>
    ): Expression {
        return when (op) {
            "abs" -> op1(args, ::abs, op)
            "asoc" -> op1(args, ::acos, op)
            "asoch" -> op1(args, ::acosh, op)
            "asin" -> op1(args, ::asin, op)
            "asinh" -> op1(args, ::asinh, op)
            "atan" -> op1(args, ::atan, op)
            "atan2" -> op2(args, ::atan2, op)
            "atanh" -> op1(args, ::atanh, op)
            "cbrt" -> op1(args, ::cbrt, op)
            "ceil" -> op1(args, ::ceil, op)
            "cos" -> op1(args, ::cos, op)
            "cosh" -> op1(args, ::cosh, op)
            "exp" -> op1(args, ::exp, op)
            "expm1" -> op1(args, ::expm1, op)
            "floor" -> op1(args, ::floor, op)
            "hypot" -> opN(args, ::hypotN, op)
            "imul" -> op2(args, ::imul, op)
            "log" -> op2(args, ::log, op)
            "log10" -> op1(args, ::log10, op)
            "log1p " -> op1(args, ::ln1p, op)
            "log2" -> op1(args, ::log2, op)
            "max" -> opN(args, List<Float>::max, op)
            "min" -> opN(args, List<Float>::min, op)
            "pow" -> op2(args, Float::pow, op)
            "random" -> OpRandomNumber()
            "round" -> op1(args, Float::roundToInt, op)
            "sign" -> op1(args, Float::sign, op)
            "sin" -> op1(args, ::sin, op)
            "sinh" -> op1(args, ::sinh, op)
            "sqrt" -> op1(args, ::sqrt, op)
            "tan" -> op1(args, ::tan, op)
            "tanh" -> op1(args, ::tanh, op)
            "trunc" -> op1(args, ::truncate, op)

            else -> OpUndefined
        }
    }

    private fun op1(args: List<Expression>, func: (Float) -> Number, name: String): Expression {
        checkArgs(args, 1, name)

        val a = args.argAt(0)
        return Expression { property, context, state ->
            val a = a(property, context, state)
            require(a is Number) {
                "Can't get Math.$name of $a"
            }
            func(a.toFloat())
        }
    }

    private fun op2(
        args: List<Expression>,
        func: (Float, Float) -> Number,
        name: String
    ): Expression {
        checkArgs(args, 2, name)

        val a = args.argAt(0)
        val b = args.argAt(1)
        return Expression { property, context, state ->
            val a = a(property, context, state)
            val b = b(property, context, state)
            require(a is Number && b is Number) {
                "Can't get Math.$name of ($a,$b)"
            }
            func(a.toFloat(), b.toFloat())
        }
    }

    private fun opN(
        args: List<Expression>,
        func: (List<Float>) -> Number,
        name: String
    ): Expression {
        check(args.isNotEmpty()){
            "Math.$name must have at least 1 argument"
        }
        return Expression { property, context, state ->

            val a = args.fastMap {
                val n = it(property, context, state).also {
                    check(it is Number) {
                        "Illegal arguments for Math.$name"
                    }
                } as Number

                n.toFloat()
            }

            func(a)
        }
    }


    private val PI = Expression { _, _, _ -> 3.1415927f }
    private val E = Expression { _, _, _ -> 2.7182817f }
    private val LN10 = Expression { _, _, _ -> 2.3025851f }
    private val LN2 = Expression { _, _, _ -> 0.6931472f }
    private val LOG10E = Expression { _, _, _ -> 0.4342945f }
    private val LOG2E = Expression { _, _, _ -> 1.442695f }
    private val SQRT1_2 = Expression { _, _, _ -> 0.70710677f }
    private val SQRT2 = Expression { _, _, _ -> 1.4142135f }
}

private fun hypotN(args : List<Float>): Float {
    return sqrt(args.fastMap(::pow2).fastSum())
}

private fun pow2(a : Float) = a * a

private fun List<Float>.fastSum() : Float {
    var x = 0f
    fastForEach { x += it }
    return x
}

private fun imul(x : Float, y : Float) : Int {
    val a = x.toInt()
    val b = y.toInt()
    val ah = (a ushr 16) and 0xffff
    val al = a and 0xffff
    val bh = (b ushr 16) and 0xffff
    val bl = b and 0xffff

    return (al * bl) + ((((ah * bl) + (al * bh)) shl 16) ushr 0) or 0
}
