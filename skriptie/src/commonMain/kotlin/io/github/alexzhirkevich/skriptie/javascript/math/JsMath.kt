package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.InterpretationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OpUndefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.random.OpRandomNumber
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.fastMap
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.fastSumBy
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext
import io.github.alexzhirkevich.skriptie.javascript.validateJsNumber
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

internal val JsInfinity =  OpConstant<JSScriptContext>(Double.POSITIVE_INFINITY)

internal object JsMath : InterpretationContext<JSScriptContext> {

    override fun invoke(context: JSScriptContext): Any = this

    override fun interpret(
        callable: String?,
        args: List<Expression<JSScriptContext>>?
    ): Expression<JSScriptContext> {
       return if (args == null){
           interpretVar(callable)
       } else {
           interpretFun(callable, args)
       }
    }

    private fun interpretVar(
        op: String?,
    ): Expression<JSScriptContext> {
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
        args: List<Expression<JSScriptContext>>
    ): Expression<JSScriptContext> {
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
            "max" -> opN(args, List<Double>::max, op)
            "min" -> opN(args, List<Double>::min, op)
            "pow" -> op2(args, Double::pow, op)
            "random" -> OpRandomNumber()
            "round" -> op1(args, Double::roundToInt, op)
            "sign" -> op1(args, Double::sign, op)
            "sin" -> op1(args, ::sin, op)
            "sinh" -> op1(args, ::sinh, op)
            "sqrt" -> op1(args, ::sqrt, op)
            "tan" -> op1(args, ::tan, op)
            "tanh" -> op1(args, ::tanh, op)
            "trunc" -> op1(args, ::truncate, op)

            else -> OpUndefined
        }
    }

    private fun op1(
        args: List<Expression<JSScriptContext>>,
        func: (Double) -> Number,
        name: String
    ): Expression<JSScriptContext> {
        checkArgs(args, 1, name)

        val a = args.argAt(0)
        return Expression {
            val a = a(it).validateJsNumber()
            require(a is Number) {
                "Can't get Math.$name of $a"
            }
            func(a.toDouble())
        }
    }

    private fun op2(
        args: List<Expression<JSScriptContext>>,
        func: (Double, Double) -> Number,
        name: String
    ): Expression<JSScriptContext> {
        checkArgs(args, 2, name)

        val a = args.argAt(0)
        val b = args.argAt(1)
        return Expression {
            val a = a(it).validateJsNumber()
            val b = b(it).validateJsNumber()
            require(a is Number && b is Number) {
                "Can't get Math.$name of ($a,$b)"
            }
            func(a.toDouble(), b.toDouble())
        }
    }

    private fun opN(
        args: List<Expression<JSScriptContext>>,
        func: (List<Double>) -> Number,
        name: String
    ): Expression<JSScriptContext> {
        check(args.isNotEmpty()){
            "Math.$name must have at least 1 argument"
        }
        return Expression {  context ->

            val a = args.fastMap {
                val n = it(context).validateJsNumber().also {
                    check(it is Number) {
                        "Illegal arguments for Math.$name"
                    }
                } as Number

                n.toDouble()
            }

            func(a)
        }
    }


    private val PI = OpConstant<JSScriptContext>(kotlin.math.PI)
    private val E = OpConstant<JSScriptContext>(kotlin.math.E)
    private val LN10 =  OpConstant<JSScriptContext>(2.302585092994046)
    private val LN2 =  OpConstant<JSScriptContext>(0.6931471805599453)
    private val LOG10E =  OpConstant<JSScriptContext>(0.4342944819032518)
    private val LOG2E =  OpConstant<JSScriptContext>(1.4426950408889634)
    private val SQRT1_2 =  OpConstant<JSScriptContext>(0.7071067811865476)
    private val SQRT2 =  OpConstant<JSScriptContext>(1.4142135623730951)
}

private fun hypotN(args : List<Double>): Double {
    return sqrt(args.fastSumBy { it * it })
}

private fun imul(x : Double, y : Double) : Long {
    val a = x.toLong().toInt()
    val b = y.toLong().toInt()

    val ah = (a ushr 16) and 0xffff
    val al = a and 0xffff
    val bh = (b ushr 16) and 0xffff
    val bl = b and 0xffff

    return ((al * bl) + ((((ah * bl) + (al * bh)) shl 16) ushr 0) or 0).toLong()
}
