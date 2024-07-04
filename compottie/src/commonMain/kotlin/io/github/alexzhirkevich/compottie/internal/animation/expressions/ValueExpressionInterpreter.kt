package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpConstant
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpGlobalContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMakeList
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpSub
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpUnaryMinus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpUnaryPlus

internal class ValueExpressionInterpreter(
    private val expr : String,
) : ExpressionInterpreter{
    private var pos = -1
    private var ch: Char = ' '

    override fun interpret(): Expression {
        pos = -1
        ch = ' '
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("Parsing $expr")
        }
        nextChar()
        val x = parseExpressionOp(OpGlobalContext)
        require(pos <= expr.length) {
            "Unexpected Lottie expression $ch"
        }
        return x.also {
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("Expression parsed: $expr")
            }
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

    private fun nextCharIs(condition : (Char) -> Boolean) : Boolean {
        var i = pos

        while (i < expr.length){
            if (condition(expr[i]))
                return true
            if (expr[i] == ' ')
                i++
            else return false
        }
        return false
    }

    private fun parseExpressionOp(context: Expression): Expression {
        var x = parseTermOp(context)
        while (true) {
            x = when {
                eat('+') -> OpAdd(x, parseTermOp(context))
                eat('-') -> OpSub(x, parseTermOp(context))
                else -> return x
            }
        }
    }

    private fun parseTermOp(context : Expression): Expression {
        var x = parseFactorOp(context)
        while (true) {
            x = when {
                eat('*') -> OpMul(x, parseFactorOp(context))
                eat('/') -> OpDiv(x, parseFactorOp(context))

                else -> return x
            }
        }
    }

    private fun parseFactorOp(context: Expression): Expression {
        return when {
            context is OpGlobalContext && eat('+') ->
                OpUnaryPlus(parseFactorOp(context))

            context is OpGlobalContext && eat('-') ->
                OpUnaryMinus(parseFactorOp(context))

            context is OpGlobalContext && eat('(') -> {
                parseExpressionOp(context).also {
                    require(eat(')')) {
                        "Bad expression: Missing ')'"
                    }
                }
            }

            context is OpGlobalContext && ch.isDigit() || nextCharIs('.'::equals) -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("making const number... ")
                }
                var dotsCount = 0
                val startPos = pos
                do {
                    nextChar()
                    if (nextCharIs('.'::equals)) {
                        require(dotsCount == 0){
                            "Invalid number at index $startPos: $expr"
                        }
                        dotsCount++
                    }
                } while (ch.isDigit() || nextCharIs('.'::equals))

                val num = expr.substring(startPos, pos).let {
                    if (dotsCount == 1) it.toFloat() else it.toInt()
                }
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println(num)
                }
                OpConstant(num)
            }
            context is OpGlobalContext && nextCharIs('\''::equals) || nextCharIs('"'::equals) -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("making const string... ")
                }
                val c = ch
                val startPos = pos
                do {
                    nextChar()
                } while (!eat(c))
                val str = expr.substring(startPos, pos).drop(1).dropLast(1)
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println(str)
                }
                return OpConstant(str)
            }

            context is OpGlobalContext && eat('[') -> { // make array
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making array... ")
                }
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
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making index... ")
                }
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
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making fun $func")
                }
                val parsedOp = when (context) {
                    is ExpressionContext<*> -> context.parse(func, args)
                    else -> error("Unsupported Lottie expression function: $func")
                }

                when {
                    // property || index
                    eat('.') || nextCharIs('['::equals) ->
                        parseFactorOp(parsedOp) // continue with receiver

                    else -> parsedOp
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


private val funMap = (('a'..'z').toList() + ('A'..'Z').toList() + '$' + '_' ).associateBy { it }

private fun Char.isFun() = funMap[this] != null