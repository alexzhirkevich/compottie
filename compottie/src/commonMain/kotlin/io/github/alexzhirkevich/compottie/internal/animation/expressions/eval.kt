package io.github.alexzhirkevich.compottie.internal.animation.expressions

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

fun eval(str: String): Double {
    return Evaluator(str).parse()
}

private class Evaluator(private val expr : String) {
    var pos = -1
    var ch = 0
    fun nextChar() {
        ch = if (++pos < expr.length) expr[pos].code else -1
    }

    fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < expr.length) throw IllegalArgumentException("Unexpected: " + ch.toChar())
        return x
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number
    //        | functionName `(` expression `)` | functionName factor
    //        | factor `^` factor
    fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm() // addition
            else if (eat('-'.code)) x -= parseTerm() // subtraction
            else return x
        }
    }

    fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor() // multiplication
            else if (eat('/'.code)) x /= parseFactor() // division
            else return x
        }
    }

    fun parseFactor(): Double {
        if (eat('+'.code)) return +parseFactor() // unary plus
        if (eat('-'.code)) return -parseFactor() // unary minus
        var x: Double
        val startPos = pos
        if (eat('('.code)) { // parentheses
            x = parseExpression()
            if (!eat(')'.code)) throw IllegalArgumentException("Missing ')'")
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
            x = expr.substring(startPos, pos).toDouble()
        } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
            while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
            val func = expr.substring(startPos, pos)
            if (eat('('.code)) {
                x = parseExpression()
                if (!eat(')'.code)) throw IllegalArgumentException("Missing ')' after argument to $func")
            } else {
                x = parseFactor()
            }
            x = when (func) {
                "sqrt" -> sqrt(x)
                "sin" -> sin(toRadians(x))
                "cos" -> cos(toRadians(x))
                "tan" -> tan(toRadians(x))
                "ln" -> ln(x)
                else -> throw IllegalArgumentException(
                    "Unknown function: $func"
                )
            }
        } else {
            throw IllegalArgumentException("Unexpected: " + ch.toChar())
        }
        if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation
        return x
    }

    fun toRadians(degree: Double): Double {
        return degree * PI / 180
    }
}