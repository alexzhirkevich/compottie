package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpGlobalContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpConstant
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMakeList
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpSub
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpUnaryMinus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpUnaryPlus
import kotlin.math.PI

internal class OperationParser(private val expr : String) {
    private var pos = -1
    private var ch: Char = ' '

    fun parse(): Operation {
        println("Parsing $expr")
        nextChar()
        val x = parseExpressionOp(OpGlobalContext)
        require(pos <= expr.length) {
            "Unexpected Lottie expression $ch"
        }
        return x.also {
            println("Expression parsed: $expr")
        }
    }


    private fun nextChar() {
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

//    private fun eat(str: String): Boolean {
//        while (ch == ' ') nextChar()
//
//        var i = pos - 1
//
//        return if (str.all { pos + ++i < expr.length && expr[pos + i] == it }) {
//            repeat(str.length) {
//                nextChar()
//            }
//            true
//        } else false
//    }


    private fun parseExpressionOp(context: Operation): Operation {
        var x = parseTermOp(context)
        while (true) {
            x = when {
                eat('+') -> OpAdd(x, parseTermOp(context))
                eat('-') -> OpSub(x, parseTermOp(context))
                else -> return x
            }
        }
    }

    private fun parseTermOp(context : Operation): Operation {
        var x = parseFactorOp(context)
        while (true) {
            x = when {
                eat('*') -> OpMul(x, parseFactorOp(context))
                eat('/') -> OpDiv(x, parseFactorOp(context))

                else -> return x
            }
        }
    }

    private fun parseFactorOp(context: Operation): Operation {
        return when {
            context is OpGlobalContext && eat('+') -> OpUnaryPlus(parseFactorOp(context))
            context is OpGlobalContext && eat('-') -> OpUnaryMinus(parseFactorOp(context))

            context is OpGlobalContext && eat('(') -> {
                parseExpressionOp(context).also {
                    require(eat(')')) {
                        "Bad expression: Missing ')'"
                    }
                }
            }

            context is OpGlobalContext && ch.isDigit() || ch == '.' -> {
                print("making number... ")
                var float = false
                val startPos = pos
                do {
                    nextChar()
                    if (!float){
                        float = ch == '.'
                    }
                } while (ch.isDigit() || ch == '.')

                val num = expr.substring(startPos, pos).let {
                    if (float) it.toFloat() else it.toInt()
                }
                println(num)
                OpConstant(num)
            }
            context is OpGlobalContext && ch == '\'' || ch == '"' -> {
                print("making string... ")
                val c = ch
                val startPos = pos
                do {
                    nextChar()
                } while (!eat(c))
                val str = expr.substring(startPos, pos).drop(1).dropLast(1)
                println(str)
                return OpConstant(str)
            }

            context is OpGlobalContext && eat('[') -> { // make array
                println("making array... ")
                val arrayArgs = buildList {
                    do {
                        if (eat(']')){ // empty list
                            return@buildList
                        }
                        add(parseExpressionOp(context))
                    } while (eat(','))
                    require(eat(']')) {
                        "Bad expression: missing ]"
                    }
                }
                OpMakeList(arrayArgs)
            }

            context !is OpGlobalContext &&  eat('[') -> { // index
                println("making index... ")
                OpIndex(context, parseExpressionOp(OpGlobalContext)).also {
                    require(eat(']')) {
                        "Bad expression: Missing ']'"
                    }
                }
            }

            ch.isFun() -> {

                val startPos = pos
                do {
                    nextChar()
                } while (ch.isFun())

                val func = expr.substring(startPos, pos)
                val args = buildList {
                    when {
                        eat('(') -> {
                            do {
                                add(parseExpressionOp(OpGlobalContext))
                            } while (eat(','))

                            require(eat(')')) {
                                "Bad expression:Missing ')' after argument to $func"
                            }
                        }
                    }
                }
                println("making fun $func")

                val f = when (context) {
                    is OperationContext -> context.evaluate(func, args)
                    else -> error("Unsupported Lottie expression function: $func")
                }

                when {
                    eat('.') || ch == '[' -> parseFactorOp(f)

                    else -> f
                }
            }

            else -> error("Unsupported Lottie expression: $expr")
        }
    }
}

internal fun checkArgs(args : List<*>, count : Int, func : String) {
    require(args.size == count){
        "$func takes $count arguments, but ${args.size} got"
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


private val floatPI = PI.toFloat()

private val funMap = (('a'..'z').toList() + ('A'..'Z').toList() + "$" ).associateBy { it }

private fun Char.isFun() = funMap[this] != null