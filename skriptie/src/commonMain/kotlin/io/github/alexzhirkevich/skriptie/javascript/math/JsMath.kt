package io.github.alexzhirkevich.skriptie.javascript.math

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.argAt
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.fastMap
import io.github.alexzhirkevich.skriptie.common.fastSumBy
import io.github.alexzhirkevich.skriptie.ecmascript.Object
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgs
import io.github.alexzhirkevich.skriptie.ecmascript.obj
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext
import io.github.alexzhirkevich.skriptie.javascript.numberOrThis
import kotlin.math.*
import kotlin.random.Random

internal fun JsMath() : Object {

    return obj<JSScriptContext>("Math") {

        "PI" eq PI
        "E" eq E
        "LN10" eq 2.302585092994046
        "LN2" eq 0.6931471805599453
        "LOG10E" eq 0.4342944819032518
        "LOG2E" eq 1.4426950408889634
        "SQRT1_2" eq 0.7071067811865476
        "SQRT2" eq 1.4142135623730951

        "abs".func("x") { op1(it, ::acos) }
        "asoc".func("x") { op1(it, ::acos) }
        "asoch".func("x") { op1(it, ::acosh) }
        "asin".func("x") { op1(it, ::asin) }
        "asinh".func("x") { op1(it, ::asinh) }
        "atan".func("x") { op1(it, ::atan) }
        "atan2".func("y", "x") { op2(it, ::atan2) }
        "atanh".func("x") { op1(it, ::atanh) }
        "cbrt".func("x") { op1(it, ::cbrt) }
        "ceil".func("x") { op1(it, ::ceil) }
        "cos".func("x") { op1(it, ::cos) }
        "cosh".func("x") { op1(it, ::cosh) }
        "exp".func("x") { op1(it, ::exp) }
        "expm1".func("x") { op1(it, ::expm1) }
        "floor".func("x") { op1(it, ::floor) }
        "hypot".func(
            "values",
            params = { FunctionParam(it, isVararg = true) }
        ) { opVararg(it, ::hypotN) }
        "imul".func("x", "y",) { op2(it, ::imul) }
        "log".func("x") { op1(it, ::ln) }
        "log10".func("x") { op1(it, ::log10) }
        "log1p ".func("x") { op1(it, ::ln1p) }
        "log2".func("x") { op1(it, ::log2) }
        "max".func(
            "values",
            params = { FunctionParam(it, isVararg = true) }
        ) {
            opVararg(it, List<Double>::max)
        }
        "min".func(
            "values",
            params = { FunctionParam(it, isVararg = true) }
        ) { opVararg(it, List<Double>::min) }
        "pow".func("x", "y",) { op2(it, Double::pow) }
        "random".func("x") { Expression { Random.nextDouble() } }
        "round".func("x") { op1(it, ::round) }
        "sign".func("x") { op1(it, ::sign) }
        "sin".func("x") { op1(it, ::sin) }
        "sinh".func("x") { op1(it, ::sinh) }
        "sqrt".func("x") { op1(it, ::sqrt) }
        "tan".func("x") { op1(it, ::tan) }
        "tanh".func("x") { op1(it, ::tanh) }
        "trunc".func("x") { op1(it, ::truncate) }
    }
}

//internal object JsMath : InterpretationContext<JSScriptContext> {
//
//    override fun invoke(context: JSScriptContext): Any = this
//
//    override fun interpret(
//        callable: String?,
//        args: List<Expression<JSScriptContext>>?
//    ): Expression<JSScriptContext> {
//       return if (args == null){
//           interpretVar(callable)
//       } else {
//           interpretFun(callable, args)
//       }
//    }
//
//    private fun interpretVar(
//        op: String?,
//    ): Expression<JSScriptContext> {
//        return when (op) {
//            "PI" -> PI
//            "E" -> E
//            "LN10" -> LN10
//            "LN2" -> LN2
//            "LOG10E" -> LOG10E
//            "LOG2E" -> LOG2E
//            "SQRT1_2" -> SQRT1_2
//            "SQRT2" -> SQRT2
//            else -> OpConstant(Unit)
//        }
//    }
//    private fun interpretFun(
//        op: String?,
//        args: List<Expression<JSScriptContext>>
//    ): Expression<JSScriptContext> {
//        return when (op) {
//            "abs" -> op1(args, ::abs, op)
//            "asoc" -> op1(args, ::acos, op)
//            "asoch" -> op1(args, ::acosh, op)
//            "asin" -> op1(args, ::asin, op)
//            "asinh" -> op1(args, ::asinh, op)
//            "atan" -> op1(args, ::atan, op)
//            "atan2" -> op2(args, ::atan2, op)
//            "atanh" -> op1(args, ::atanh, op)
//            "cbrt" -> op1(args, ::cbrt, op)
//            "ceil" -> op1(args, ::ceil, op)
//            "cos" -> op1(args, ::cos, op)
//            "cosh" -> op1(args, ::cosh, op)
//            "exp" -> op1(args, ::exp, op)
//            "expm1" -> op1(args, ::expm1, op)
//            "floor" -> op1(args, ::floor, op)
//            "hypot" -> opN(args, ::hypotN, op)
//            "imul" -> op2(args, ::imul, op)
//            "log" -> op2(args, ::log, op)
//            "log10" -> op1(args, ::log10, op)
//            "log1p " -> op1(args, ::ln1p, op)
//            "log2" -> op1(args, ::log2, op)
//            "max" -> opN(args, List<Double>::max, op)
//            "min" -> opN(args, List<Double>::min, op)
//            "pow" -> op2(args, Double::pow, op)
//            "random" -> Expression { Random.nextDouble() }
//            "round" -> op1(args, Double::roundToInt, op)
//            "sign" -> op1(args, Double::sign, op)
//            "sin" -> op1(args, ::sin, op)
//            "sinh" -> op1(args, ::sinh, op)
//            "sqrt" -> op1(args, ::sqrt, op)
//            "tan" -> op1(args, ::tan, op)
//            "tanh" -> op1(args, ::tanh, op)
//            "trunc" -> op1(args, ::truncate, op)
//
//            else -> OpConstant(Unit)
//        }
//    }
//
//
//
//
//    private val PI = OpConstant<JSScriptContext>(kotlin.math.PI)
//    private val E = OpConstant<JSScriptContext>(kotlin.math.E)
//    private val LN10 =  OpConstant<JSScriptContext>(2.302585092994046)
//    private val LN2 =  OpConstant<JSScriptContext>(0.6931471805599453)
//    private val LOG10E =  OpConstant<JSScriptContext>(0.4342944819032518)
//    private val LOG2E =  OpConstant<JSScriptContext>(1.4426950408889634)
//    private val SQRT1_2 =  OpConstant<JSScriptContext>(0.7071067811865476)
//    private val SQRT2 =  OpConstant<JSScriptContext>(1.4142135623730951)
//}

private fun <C: ScriptContext> op1(
    args: List<Expression<C>>,
    func: (Double) -> Number
): Expression<C> {
    checkArgs(args, 1, "")

    val a = args.argAt(0)

    return Expression {
        var a = a(it)?.numberOrThis() ?: 0.0

        if (a !is Number){
           a = a.toString().toDoubleOrNull() ?: return@Expression Double.NaN
        }

        if (a is Number){
            func(a.toDouble())
        } else {
            Double.NaN
        }
    }
}

private fun <C: ScriptContext> op2(
    args: List<Expression<C>>,
    func: (Double, Double) -> Number,
): Expression<C> {
    checkArgs(args, 2, "")

    val a = args.argAt(0)
    val b = args.argAt(1)
    return Expression {
        var a = a(it)?.numberOrThis() ?: 0.0
        var b = b(it)?.numberOrThis() ?: 0.0
        if (a !is Number){
            a = a.toString().toDoubleOrNull() ?: return@Expression Double.NaN
        }
        if (b !is Number){
            b = b.toString().toDoubleOrNull() ?: return@Expression Double.NaN
        }
        if (a is Number && b is Number){
            func(a.toDouble(), b.toDouble())
        } else {
            Double.NaN
        }
    }
}

private fun <C: ScriptContext> opVararg(
    args: List<Expression<C>>,
    func: (List<Double>) -> Number,
): Expression<C> {
    check(args.isNotEmpty()){

    }
    return Expression {  context ->
        val a = (args[0].invoke(context) as List<*>).fastMap {
            when (it){
                is Number -> it.toDouble()
                null -> 0.0
                else -> {
                    it.toString().toDoubleOrNull() ?: return@Expression Double.NaN
                }
            }
        }
        func(a)
    }
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
