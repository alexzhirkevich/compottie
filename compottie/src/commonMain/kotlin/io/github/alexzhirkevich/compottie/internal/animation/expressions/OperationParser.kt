package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.math.PI

internal class OperationParser(private val expr : String) {
    private var pos = -1
    private var ch: Char = ' '

    fun parse(): Operation {
        nextChar()
        val x = parseExpressionOp()
        require(pos <= expr.length) {
            "Unexpected Lottie expression $ch"
        }
        return x
    }


    private fun nextChar()  {
        ch = if (++pos < expr.length) expr[pos] else ';'
    }

    private fun eat(charToEat: Char): Boolean {
        while (ch == ' ') nextChar()

        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number
    //        | functionName `(` expression `)` | functionName factor
    //        | factor `^` factor


    private fun parseExpressionOp(): Operation {
        var x = parseTermOp()
        while (true) {
            x = when {
                eat('+') -> LottieOp.op(x, parseTermOp(), LottieOp::sum)
                eat('-') -> LottieOp.op(x, parseTermOp(), LottieOp::sub)
                else -> return x
            }
        }
    }

    private fun parseTermOp(): Operation {
        var x = parseFactorOp()
        while (true) {
            x = when {
                eat('*') -> {
                    LottieOp.op(x, parseFactorOp(), LottieOp::mul)
                }

                eat('/') -> {
                    LottieOp.op(x, parseFactorOp(), LottieOp::div)
                }

                else -> return x
            }
        }
    }

    private fun parseFactorOp(): Operation {
        if (eat('+')) {
            val factor = parseFactorOp()
            return Operation { v, vars, s ->
                +(factor(v, vars, s) as Number).toFloat()
            }
        }
        if (eat('-')) {
            val factor = parseFactorOp()
            return Operation { v, vars, s ->
                -(factor(v, vars, s) as Number).toFloat()
            }
        }

        return when {
            eat('(') -> {
                val exp = parseExpressionOp()
                require(eat(')')) {
                    "Lottie expression: Missing ')'"
                }
                exp
            }

            ch.isDigit() || ch == '.' -> {
                val startPos = pos
                do {
                    nextChar()
                } while(ch.isDigit() || ch == '.')

                val num = expr.substring(startPos, pos).toFloat()

                Operation { _, _, _ -> num }
            }

            eat('[') -> {
                val arrayArgs = buildList {
                    do {
                        add(parseExpressionOp())
                    } while (eat(','))
                    require(eat(']')) {
                        "Missing ] in list creation"
                    }
                }
                makeList(arrayArgs)
            }
            ch.isFun(false) -> {
                val startPos = pos
                var inIndex = ch == '['
                while (ch.isFun(inIndex)) {
                    if (!inIndex) {
                        inIndex = ch == '['
                    } else if (inIndex) {
                        inIndex = ch != ']'
                    }
                    nextChar()
                }
                val func = expr.substring(startPos, pos)
                val args = buildList {
                    if (eat('(')) {
                        do {
                            add(parseExpressionOp())
                        } while (eat(','))

                        require(eat(')')) {
                            ("Missing ')' after argument to $func")
                        }
                    }
//                    else {
//                        try {
//                            add(parseFactorOp())
//                        } catch (t: Throwable) {
//                        }
//                    }
                }

                when (func) {
                    "add" -> {
                        checkArgs(args, 2, func)
                        LottieOp.op(args[0], args[1], LottieOp::sum)
                    }
                    "sub" -> {
                        checkArgs(args, 2, func)
                        LottieOp.op(args[0], args[1], LottieOp::sub)
                    }
                    "mul" -> {
                        checkArgs(args, 2, func)
                        LottieOp.op(args[0], args[1], LottieOp::mul)
                    }
                    "div" -> {
                        checkArgs(args, 2, func)
                        LottieOp.op(args[0], args[1], LottieOp::div)
                    }
                    "mod" -> {
                        checkArgs(args, 2, func)
                        LottieOp.op(args[0], args[1], LottieOp::mod)
                    }
                    "clamp" -> {
                        checkArgs(args, 3, func)
                        LottieOp.op(args[0], args[1], args[2], LottieOp::clamp)
                    }
                    "Math.sqrt" -> {
                        checkArgs(args, 1, func)
                        LottieOp.op(args[0], LottieOp::sqrt)
                    }
                    "Math.sin" -> {
                        checkArgs(args, 1, func)
                        LottieOp.op(args[0], LottieOp::sin)
                    }
                    "Math.cos" -> {
                        checkArgs(args, 1, func)
                        LottieOp.op(args[0], LottieOp::cos)
                    }
                    "Math.PI" -> Operation { _, _, _ -> floatPI }
                    "time" -> Operation { _, _, s -> s.time }

//                    "tan" -> tan(toRadians(x))
//                    "ln" -> ln(x)
                    else -> {
                        require(args.isEmpty()) {
                            "Unsupported Lottie expression function: $func"
                        }

                        when {
                            // value without expression
                            func.startsWith("value") -> {
                                val idx = variableIdx(func)
                                return Operation { v, _, _ ->
                                    LottieOp.index(v, idx)
                                }
                            }
                        }

                        val property = func.split(".")

                        return when (property[0]) {
                            "thisLayer" -> thisLayer(func)
                            "thisComp" -> thisComp(func)
                            "thisProperty" -> thisProperty(func)
                            else -> {
                                require(property.size == 1){
                                    "Unknown Lottie expression: $func"
                                }

                                Operation { v, vars, s ->
                                    TODO()
                                }
                            }
                        }
                    }
                }
            }

            else -> error("Unsupported Lottie expression")
        }
    }

    private fun thisProperty(property: String): Operation {
        TODO()
    }

    private fun thisLayer(property: String): Operation {
        TODO()
    }

    private fun thisComp(property: String): Operation {
        TODO()
    }

    private fun checkArgs(args : List<*>, count : Int, func : String) {
        require(args.size == count){
            "$func takes $count arguments, but ${args.size} got"
        }
    }

    fun toRadians(degree: Double): Double {
        return degree * PI / 180
    }
}

internal fun variableIdx(prop : String) : Int? {
    if (!prop.endsWith(']') || '[' !in prop) {
        return null
    }

    val idx = prop
        .substringAfterLast("[")
        .substringBeforeLast("]")

    return idx.toInt()
}

private fun makeList(ops : List<Operation>) : Operation {
    return Operation { v, vars, s ->
        val args = ops.fastMap { it.invoke(v, vars, s) }

        if ((args.size == 2 || args.size == 3) && args.all { it is Number }) {
            return@Operation Vec2((args[0] as Number).toFloat(), (args[1] as Number).toFloat())
        }

        error("Can't make a list of $args")
    }
}

private val floatPI = PI.toFloat()

private val funMap = (('a'..'z').toList() + ('A'..'Z').toList() + '.' + '[' +']').associateBy { it }

private fun Char.isFun(inIndex : Boolean) = funMap[this] != null || inIndex && isDigit()