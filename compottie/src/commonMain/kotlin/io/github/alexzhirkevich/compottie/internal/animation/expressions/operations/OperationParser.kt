package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan

internal fun parse(expression: String) : Operation =
    OperationParser(expression).parse()


private class OperationParser(private val expr : String) {
    private var pos = -1
    private var ch : Char = ' '

    fun parse(): Operation {
        nextChar()
        val x = parseExpressionOp()
        require (pos >= expr.length) {
            "Unexpected Lottie expression $ch"
        }
        return x
    }


    private fun nextChar() {
        ch = if (++pos < expr.length) expr[pos] else ' '
    }

    private fun eat(charToEat: Char): Boolean {
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
            if (eat('+')) { // addition
                val factor = parseFactorOp()
                val tx = x
                x = Operation { v, s ->
                    Op.sum(tx(v, s), factor(v, s))
                }
            } else if (eat('-')) {// subtraction
                val factor = parseFactorOp()
                val tx = x
                x = Operation { v, s ->
                    Op.sub(tx(v, s), factor(v, s))
                }
            } else return x
        }
    }

    private fun parseTermOp(): Operation {
        var x = parseFactorOp()
        while (true) {
            if (eat('*')) {  // multiplication
                val factor = parseFactorOp()
                val tx = x
                x = Operation { v, s ->
                    Op.mul(tx(v,s), factor(v,s))
                }
            }
            else if (eat('/'))  {
                val factor = parseFactorOp()
                val tx = x
                x = Operation { v, s ->
                    Op.div(tx(v,s), factor(v,s))
                }
            }
            else return x
        }
    }

    private fun parseFactorOp(): Operation {
        if (eat('+')) {
            val factor = parseFactorOp()
            Operation { v, s ->
                +(factor(v, s) as Number).toFloat()
            }
        }
        if (eat('-')) {
            val factor = parseFactorOp()
            Operation { v, s ->
                -(factor(v, s) as Number).toFloat()
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

            ch in '0'..'9' || ch == '.' -> {
                val startPos = pos
                while (ch in '0'..'9' || ch == '.') nextChar()
                val num = expr.substring(startPos, pos).toDouble()
                Operation { _, _ -> num }
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
                val args = mutableListOf<Operation>()
                if (eat('(')) {
                    args += parseExpressionOp()
                    do {
                        args += parseExpressionOp()
                    } while (ch == ',')

                    if (!eat(')')) throw IllegalArgumentException("Missing ')' after argument to $func")
                } else {
                    try {
                        args += parseFactorOp()
                    } catch (t: Throwable) {
                        // function without arguments such as Math.PI
                    }
                }

                when (func) {
                    "add" -> Operation { v, s ->
                        Op.sum(args[0](v, s), args[1](v, s))
                    }

                    "sqrt" -> Operation { v, s ->
                        Op.sqrt(args[0](v, s))
                    }

                    "Math.sin" -> Operation { v, s ->
                        Op.sin(args[0](v, s))
                    }

                    "Math.cos" -> Operation { v, s ->
                        Op.cos(args[0](v, s))
                    }

                    "mod" -> Operation { v, s ->
                        Op.mod(args[0](v, s), args[1](v, s))
                    }
                    "Math.PI" -> Operation { _, _ -> floatPI }

//                    "tan" -> tan(toRadians(x))
//                    "ln" -> ln(x)
                    else -> throw IllegalArgumentException(
                        "Unknown function: $func"
                    )
                }
                TODO()
            }

            else -> error("Unsupported Lottie expression")
        }
    }

    fun toRadians(degree: Double): Double {
        return degree * PI / 180
    }
}

private val floatPI = PI.toFloat()

private val funMap = (('a'..'z').toList() + ('A'..'Z').toList() + '.' + '[' +']').associateBy { it }

private fun Char.isFun(inIndex : Boolean) = funMap[this] != null || inIndex && isDigit()