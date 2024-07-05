package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAssign
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpAssignByIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpBlock
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpConstant
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpEquals
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpGetVariable
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpGlobalContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpIfCondition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMakeArray
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpNot
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpSub
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpUnaryMinus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpUnaryPlus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpVar

internal class SingleExpressionInterpreter(
    private val expr : String,
) : ExpressionInterpreter {
    private var pos = -1
    private var ch: Char = ' '

    override fun interpret(): Expression {
        pos = -1
        ch = ' '
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("Parsing $expr")
        }
        nextChar()
        val x = parseAssignment(OpGlobalContext)
        require(pos <= expr.length) {
            "Unexpected Lottie expression $expr"
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

    private fun nextCharIs(condition: (Char) -> Boolean): Boolean {
        var i = pos

        while (i < expr.length) {
            if (condition(expr[i]))
                return true
            if (expr[i] == ' ')
                i++
            else return false
        }
        return false
    }

    private fun eatSequence(seq : String): Boolean {

        val p = pos
        val c = ch

        if (seq.isEmpty())
            return true

        if (!eat(seq[0])) {
            return false
        }

        return if (expr.indexOf(seq, startIndex = pos-1) == pos-1){
            seq.drop(1).forEach(::eat)
            true
        } else {
            pos = p
            ch = c
            false
        }
    }

    private fun nextSequenceIs(seq : String): Boolean {

        val p = pos
        val c = ch

        if (seq.isEmpty())
            return true

        if (!eat(seq[0])) {
            return false
        }

        return if (expr.indexOf(seq, startIndex = pos-1) == pos-1){
            seq.drop(1).forEach(::eat)
            pos = p
            ch = c
            true
        } else {
            pos = p
            ch = c
            false
        }
    }

    private fun parseAssignment(context: Expression): Expression {
        var x = parseExpressionOp(context)
        if (EXPR_DEBUG_PRINT_ENABLED){
            println("Parsed ${x::class} as assignment")
        }
        while (true) {
            x = when {
                eatSequence("+=") -> parseAssignmentValue(x, OpAdd.Companion::invoke)
                eatSequence("-=") -> parseAssignmentValue(x, OpSub.Companion::invoke)
                eatSequence("*=") -> parseAssignmentValue(x, OpMul.Companion::invoke)
                eatSequence("/=") -> parseAssignmentValue(x, OpDiv.Companion::invoke)
                eat('=') -> parseAssignmentValue(x, null)
                else -> return x
            }
        }
    }

    private fun parseAssignmentValue(x : Expression, merge : ((Any, Any) -> Any)? = null) =  when {
        x is OpIndex && x.variable is OpGetVariable -> OpAssignByIndex(
            variableName = x.variable.name,
            index = x.index,
            assignableValue = parseExpressionOp(OpGlobalContext),
            merge = merge
        ).also {
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("parsing assignment with index for ${x.variable.name}")
            }
        }

        x is OpGetVariable -> OpAssign(
            variableName = x.name,
            assignableValue = parseExpressionOp(OpGlobalContext),
            merge = merge
        ).also {
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("parsing assignment for ${x.name}")
            }
        }

        else -> error("Invalid assignment")
    }

    private fun parseExpressionOp(context: Expression): Expression {
        var x = parseTermOp(context)
        while (true) {
            x = when {
                eatSequence("===") -> OpEquals(x, parseExpressionOp(OpGlobalContext), true)
                eatSequence("==") -> OpEquals(x, parseExpressionOp(OpGlobalContext), false)
                eatSequence("!=") -> OpNot(OpEquals(x, parseExpressionOp(OpGlobalContext), false))
                !nextSequenceIs("+=") && eat('+') -> OpAdd(x, parseTermOp(context))
                !nextSequenceIs("-=") && eat('-') -> OpSub(x, parseTermOp(context))
                else -> return x
            }
        }
    }

    private fun parseTermOp(context: Expression): Expression {
        var x = parseFactorOp(context)
        while (true) {
            x = when {
                !nextSequenceIs("*=") && eat('*') -> OpMul(x, parseFactorOp(context))
                !nextSequenceIs("/=") && eat('/') -> OpDiv(x, parseFactorOp(context))
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

            context is OpGlobalContext && !nextSequenceIs("!=") && eat('!') ->
                OpNot(parseExpressionOp(context))

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
                        require(dotsCount == 0) {
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
                        if (eat(']')) { // empty list
                            return@buildList
                        }
                        add(parseExpressionOp(context))
                    } while (eat(','))
                    require(eat(']')) {
                        "Bad expression: missing ]"
                    }
                }
                OpMakeArray(arrayArgs)
            }

            context !is OpIfCondition && context !is OpGlobalContext && eat('[') -> { // index
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making index... ")
                }
                OpIndex(context, parseExpressionOp(OpGlobalContext)).also {
                    require(eat(']')) {
                        "Bad expression: Missing ']'"
                    }
                }
            }

            context is OpIfCondition -> {

                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("parsing if...")
                }

                context.onTrue = parseBlock()

                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("parsed onTrue...")
                }

                if (eatSequence("else")) {
                    context.onFalse = parseBlock()
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("parsed onFalse...")
                    }
                }

                context
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
                    is ExpressionContext<*> -> context.interpret(func, args)
                    else -> error("Unsupported Lottie expression function: $func")
                }

                when {
                    // begin condition || property || index
                    parsedOp is OpVar ||
                    parsedOp is OpIfCondition
                            || eat('.')
                            || nextCharIs('['::equals) ->
                        parseFactorOp(parsedOp) // continue with receiver

                    else -> parsedOp
                }
            }

            else -> error("Unsupported Lottie expression: $expr")
        }
    }

    private fun parseBlock(): Expression {
        val list =  buildList {
            if (eat('{')) {
                while (!eat('}') && pos < expr.length) {
                    add(parseAssignment(OpGlobalContext))
                    eat(';')
                }
            } else {
                add(parseAssignment(OpGlobalContext))
            }
        }
        return OpBlock(list)
    }
}

internal fun checkArgs(args : List<*>, count : Int, func : String) {
    require(args.size == count){
        "$func takes $count arguments, but ${args.size} got"
    }
}


private val funMap = (('a'..'z').toList() + ('A'..'Z').toList() + '$' + '_' ).associateBy { it }

private fun Char.isFun() = funMap[this] != null

private fun Char.isSignExpression() = funMap[this] != null