package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.common.Callable
import io.github.alexzhirkevich.skriptie.common.Delegate
import io.github.alexzhirkevich.skriptie.common.Function
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.Named
import io.github.alexzhirkevich.skriptie.common.OpAssign
import io.github.alexzhirkevich.skriptie.common.OpAssignByIndex
import io.github.alexzhirkevich.skriptie.common.OpBlock
import io.github.alexzhirkevich.skriptie.common.OpBreak
import io.github.alexzhirkevich.skriptie.common.OpCompare
import io.github.alexzhirkevich.skriptie.common.OpConstant
import io.github.alexzhirkevich.skriptie.common.OpContinue
import io.github.alexzhirkevich.skriptie.common.OpDoWhileLoop
import io.github.alexzhirkevich.skriptie.common.OpEquals
import io.github.alexzhirkevich.skriptie.common.OpEqualsComparator
import io.github.alexzhirkevich.skriptie.common.OpForLoop
import io.github.alexzhirkevich.skriptie.common.OpFunctionExec
import io.github.alexzhirkevich.skriptie.common.OpGetVariable
import io.github.alexzhirkevich.skriptie.common.OpGreaterComparator
import io.github.alexzhirkevich.skriptie.common.OpIfCondition
import io.github.alexzhirkevich.skriptie.common.OpIncDecAssign
import io.github.alexzhirkevich.skriptie.common.OpIndex
import io.github.alexzhirkevich.skriptie.common.OpLessComparator
import io.github.alexzhirkevich.skriptie.common.OpLongInt
import io.github.alexzhirkevich.skriptie.common.OpLongLong
import io.github.alexzhirkevich.skriptie.common.OpMakeArray
import io.github.alexzhirkevich.skriptie.common.OpNot
import io.github.alexzhirkevich.skriptie.common.OpReturn
import io.github.alexzhirkevich.skriptie.common.OpTryCatch
import io.github.alexzhirkevich.skriptie.common.OpWhileLoop
import io.github.alexzhirkevich.skriptie.common.ThrowableValue
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.isAssignable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.pow

internal val EXPR_DEBUG_PRINT_ENABLED = true
internal enum class LogicalContext {
    And, Or, Compare
}

internal enum class BlockContext {
    None, Loop, Function, Class
}

internal class ESInterpreterImpl(
    expr : String,
    private val langContext: LangContext,
    private val globalContext : InterpretationContext,
) {
    private var expr = "{$expr}"
    private var pos = -1
    private var ch: Char = ' '

    fun interpret(): Script {
        val block = parseBlock(scoped = false, blockContext = emptyList())
        return Script {
            try {
                langContext.toKotlin(block(it))
            } catch (t: Throwable) {
                if (t is ESAny) {
                    throw t
                } else {
                    throw ESError(t.message, t)
                }
            }
        }
    }

    private fun prepareNextChar() {
        while (ch.skip() && pos < expr.length) {
            nextChar()
        }
    }

    private fun nextChar() {
        ch = if (++pos < expr.length) expr[pos] else ' '
    }

    private fun prevChar() {
        ch = if (--pos > 0 && pos < expr.length) expr[pos] else ' '
    }

    private fun Char.skip(): Boolean = this == ' ' || this == '\n'

    private fun eat(charToEat: Char): Boolean {
        while (ch.skip() && pos < expr.length)
            nextChar()

        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    private fun eatAndExpectNot(charToEat: Char, nextCharIs : (Char) -> Boolean): Boolean {
        while (ch.skip() && pos < expr.length)
            nextChar()

        if (ch == charToEat) {
            nextChar()
            if (nextCharIs(ch)){
                prevChar()
                return false
            }
            return true
        }
        return false
    }

    private fun nextCharIs(condition: (Char) -> Boolean): Boolean {
        var i = pos

        while (i < expr.length) {
            if (condition(expr[i]))
                return true
            if (expr[i].skip())
                i++
            else return false
        }
        return false
    }

    private fun eatSequence(seq: String): Boolean {

        val p = pos
        val c = ch

        if (seq.isEmpty())
            return true

        if (!eat(seq[0])) {
            return false
        }

        return if (expr.indexOf(seq, startIndex = pos - 1) == pos - 1) {
            pos += seq.length - 1
            ch = expr[pos.coerceIn(expr.indices)]
            true
        } else {
            pos = p
            ch = c
            false
        }
    }

    private fun eatSequenceAndExpectNot(seq: String, nextChar : (Char) -> Boolean): Boolean {

        val p = pos
        val c = ch

        if (seq.isEmpty())
            return true

        if (!eat(seq[0])) {
            return false
        }

        return if (expr.indexOf(seq, startIndex = pos - 1) == pos - 1) {
            pos += seq.length - 1
            ch = expr[pos.coerceIn(expr.indices)]
            if (!nextChar(ch)) {
                true
            } else {
                pos = p
                ch = c
                false
            }
        } else {
            pos = p
            ch = c
            false
        }
    }

    private fun nextSequenceIs(seq: String): Boolean {

        val p = pos
        val c = ch

        if (seq.isEmpty())
            return true

        if (!eat(seq[0])) {
            return false
        }

        return if (expr.indexOf(seq, startIndex = pos - 1) == pos - 1) {
            pos = p
            ch = c
            true
        } else {
            pos = p
            ch = c
            false
        }
    }

    private fun parseAssignment(
        context: Expression,
        blockContext: List<BlockContext>,
        unaryOnly: Boolean = false,
        isExpressionStart: Boolean = false,
        variableName : String? = null
    ): Expression {
        var x = if (variableName == null) {
            parseExpressionOp(
                context = context,
                blockContext = blockContext,
                isExpressionStart = isExpressionStart,
                factorOnly = unaryOnly
            )
        } else {
            OpGetVariable(variableName, receiver = null)
        }
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("Parsing assignment for $x")
        }

        val checkAssignment = {
            syntaxCheck(!unaryOnly){
                "Invalid left-hand side in assignment"
            }
        }

        while (true) {
            prepareNextChar()
            x = when {
                eatSequence("+=") -> {
                    checkAssignment()
                    parseAssignmentValue(x, langContext::sum)
                }

                eatSequence("-=") -> {
                    checkAssignment()
                    parseAssignmentValue(x, langContext::sub)
                }

                eatSequence("*=") -> {
                    checkAssignment()
                    parseAssignmentValue(x, langContext::mul)
                }

                eatSequence("/=") -> {
                    checkAssignment()
                    parseAssignmentValue(x, langContext::div)
                }

                eatSequence("%=") -> {
                    checkAssignment()
                    parseAssignmentValue(x, langContext::mod)
                }

                eatSequence("&=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a,b ->
                        langContext.toNumber(a).toLong() and langContext.toNumber(b).toLong()
                    }
                }

                eatSequence("&&=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a,b ->
                        if (langContext.isFalse(a)) a else b
                    }
                }

                eatSequence("|=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a,b ->
                        langContext.toNumber(a).toLong() or langContext.toNumber(b).toLong()
                    }
                }

                eatSequence("||=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a,b ->
                        if (langContext.isFalse(a)) b else a
                    }
                }

                eatSequence("^=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a,b ->
                        langContext.toNumber(a).toLong() xor langContext.toNumber(b).toLong()
                    }
                }

                eatSequence(">>>=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a, b ->
                        langContext.toNumber(a).toLong() ushr langContext.toNumber(b).toInt()
                    }
                }

                eatSequence(">>=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a, b ->
                        langContext.toNumber(a).toLong() shr langContext.toNumber(b).toInt()
                    }
                }

                eatSequence("<<=") -> {
                    checkAssignment()
                    parseAssignmentValue(x) { a, b ->
                        langContext.toNumber(a).toLong() shl langContext.toNumber(b).toInt()
                    }
                }

                eatSequence("=>") -> OpConstant(parseArrowFunction(listOf(x), blockContext))

                eatAndExpectNot('=', '='::equals) -> {
                    checkAssignment()
                    parseAssignmentValue(x, null)
                }

                eatSequence("++") -> {
                    syntaxCheck(x.isAssignable()) {
                        "Value is not assignable"
                    }
                    OpIncDecAssign(
                        variable = x,
                        preAssign = false,
                        op = langContext::inc
                    )
                }

                eatSequence("--") -> {
                    syntaxCheck(x.isAssignable()) {
                        "Value is not assignable"
                    }
                    OpIncDecAssign(
                        variable = x,
                        preAssign = false,
                        op = langContext::dec
                    )
                }

                eat('?') -> {
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("making ternary operator: onTrue...")
                    }

                    val bContext = blockContext.dropLastWhile { it == BlockContext.Class }
                    val onTrue = parseAssignment(globalContext, bContext)

                    if (!eat(':')) {
                        throw SyntaxError("Unexpected end of input")
                    }
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("making ternary operator: onFalse...")
                    }
                    val onFalse = parseAssignment(globalContext, bContext)

                    OpIfCondition(
                        condition = x,
                        onTrue = onTrue,
                        onFalse = onFalse,
                        expressible = true
                    )
                }

                else -> return x
            }
        }
    }

    private fun parseAssignmentValue(x: Expression, merge: ((Any?, Any?) -> Any?)? = null) =
        when {
            x is OpIndex && x.variable is OpGetVariable -> OpAssignByIndex(
                variableName = x.variable.name,
                scope = x.variable.assignmentType,
                index = x.index,
                assignableValue = parseAssignment(globalContext, emptyList()),
                merge = merge
            ).also {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("parsed assignment with index for ${x.variable.name}")
                }
            }

            x is OpGetVariable -> OpAssign(
                variableName = x.name,
                receiver = x.receiver,
                assignableValue = parseAssignment(globalContext, emptyList(),),
                type = x.assignmentType,
                merge = merge
            ).also {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("parsed assignment for ${x.name}")
                }
            }

            else -> error("Invalid assignment")
        }

    private fun parseExpressionOp(
        context: Expression,
        logicalContext: LogicalContext? = null,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false,
        factorOnly : Boolean = false
    ): Expression {
        var x = if (factorOnly) {
            parseFactorOp(context, blockContext, isExpressionStart)
        } else {
            parseOperator3(context, logicalContext, blockContext, isExpressionStart)
        }
        while (true){
            prepareNextChar()
            x = when {
                eatSequence("in ") -> {
                    val obj = parseExpressionOp(context, null, blockContext, isExpressionStart)
                    val tx = x
                    Expression {
                        val o = obj(it)
                        syntaxCheck(o is ESAny){
                            "Illegal usage of 'in' operator"
                        }
                        o.contains(tx(it))
                    }
                }
                else -> return x
            }
        }
    }

    private fun parseOperator3(
        context: Expression,
        logicalContext: LogicalContext? = null,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false
    ): Expression {
        var x = parseOperator2(context, logicalContext, blockContext, isExpressionStart)
        while (true) {
            prepareNextChar()
            x = when {
                eatSequenceAndExpectNot("<<", '='::equals) -> OpLongInt(
                    x,
                    parseOperator3(globalContext, LogicalContext.Compare, blockContext),
                    Long::shl
                )

                eatSequenceAndExpectNot(">>>", '='::equals) -> OpLongInt(
                    x,
                    parseOperator3(globalContext, LogicalContext.Compare, blockContext),
                    Long::ushr
                )

                eatSequenceAndExpectNot(">>") { it == '>' || it == '=' } -> OpLongInt(
                    x,
                    parseOperator3(globalContext, LogicalContext.Compare, blockContext),
                    Long::shr
                )

                else -> return x
            }
        }
    }

    private fun parseOperator2(
        context: Expression,
        logicalContext: LogicalContext? = null,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false
    ): Expression {
        var x = parseOperator1(context, blockContext, isExpressionStart)

        while (true) {
            prepareNextChar()
            x = when {
                logicalContext != LogicalContext.Compare && eatSequenceAndExpectNot("&&", '='::equals) -> {
                    val a = x
                    val b = parseOperator2(globalContext, LogicalContext.And, blockContext)
                    Expression {
                        !langContext.isFalse(a(it)) && !langContext.isFalse(b(it))
                    }
                }

                logicalContext == null && eatSequenceAndExpectNot("||", '='::equals) -> {
                    val a = x
                    val b = parseOperator2(globalContext, LogicalContext.And, blockContext)
                    Expression {
                        !langContext.isFalse(a(it)) || !langContext.isFalse(b(it))
                    }
                }

                eatAndExpectNot('&') { it == '&' || it == '=' } -> OpLongLong(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    Long::and
                )

                eatAndExpectNot('|') { it == '|'  || it == '=' } -> OpLongLong(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    Long::or
                )

                eatAndExpectNot('^', '='::equals) -> OpLongLong(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    Long::xor
                )

                eatSequence("<=") -> OpCompare(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext)
                ) { a, b,r ->
                    OpLessComparator(a, b,r) || OpEqualsComparator(a, b,r)
                }

                eatAndExpectNot('<', '<'::equals) -> OpCompare(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    OpLessComparator
                )

                eatSequence(">=") -> OpCompare(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext)
                ) { a, b,r  ->
                    OpGreaterComparator(a, b, r) || OpEqualsComparator(a, b,r)
                }

                eatAndExpectNot('>', '>'::equals) -> OpCompare(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    OpGreaterComparator
                )

                eatSequence("===") -> OpEquals(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    true
                )

                eatSequence("==") -> OpEquals(
                    x,
                    parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                    false
                )

                eatSequence("!==") -> OpNot(
                    OpEquals(
                        x,
                        parseExpressionOp(globalContext, LogicalContext.Compare, blockContext),
                        false
                    ),
                    langContext::isFalse
                )

                eatSequence("!=") -> OpNot(
                    OpEquals(
                        x,
                        parseOperator2(globalContext, LogicalContext.Compare, blockContext),
                        true
                    ),
                    langContext::isFalse
                )

                eatAndExpectNot('+') { it == '+' || it == '=' } ->
                    Delegate(x, parseOperator2(globalContext, null,blockContext), langContext::sum)

                eatAndExpectNot('-') { it == '-' || it == '=' } ->
                    Delegate(x, parseOperator2(globalContext, null,blockContext), langContext::sub)

                else -> return x
            }
        }
    }

    private fun parseOperator1(
        context: Expression,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false
    ): Expression {
        var x = parseTermOp(context, blockContext, isExpressionStart)
        while (true) {
            prepareNextChar()
            x = when {
                eatAndExpectNot('*') { it == '*' || it == '='} -> Delegate(
                    x,
                    parseTermOp(globalContext, blockContext),
                    langContext::mul
                )

                eatAndExpectNot('/', '='::equals) -> Delegate(
                    x,
                    parseTermOp(globalContext, blockContext),
                    langContext::div
                )

                eatAndExpectNot('%', '='::equals) -> Delegate(
                    x,
                    parseTermOp(globalContext, blockContext),
                    langContext::mod
                )

                else -> return x
            }
        }
    }

    private fun parseTermOp(
        context: Expression,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false
    ): Expression {
        var x = parseFactorOp(context, blockContext, isExpressionStart)

        while (true) {
            prepareNextChar()
            x = when {
                // unique operator with right associativity
                eatSequence("**") -> {
                    val tx = x
                    val degree = parseTermOp(globalContext, blockContext)
                    Expression {
                        val xn = langContext.toNumber(tx(it)).toDouble()
                        val degreeN = langContext.toNumber(degree(it)).toDouble()
                        xn.pow(degreeN)
                    }
                }

                else -> return x
            }
        }
    }


    private fun parseFactorOp(
        context: Expression,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false,
        allowContinueWithContext : Boolean = true
    ): Expression {
        val parsedOp = when {

            isExpressionStart && nextCharIs('{'::equals) ->
                parseBlock(blockContext = emptyList())

            !isExpressionStart && eat('{') -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making object")
                }

                parseObject()
            }

            eatSequence("++") -> {
                val start = pos
                val variable = parseFactorOp(globalContext, blockContext)
                require(variable.isAssignable()) {
                    "Unexpected '++' at $start"
                }
                OpIncDecAssign(
                    variable = variable,
                    preAssign = true,
                    op = langContext::inc
                )
            }

            eatSequence("--") -> {
                val start = pos
                val variable = parseFactorOp(globalContext, blockContext)
                require(variable.isAssignable()) {
                    "Unexpected '--' at $start"
                }
                OpIncDecAssign(
                    variable = variable,
                    preAssign = true,
                    op = langContext::dec
                )
            }

            eat('+') ->
                Delegate(parseFactorOp(context, blockContext), langContext::pos)

            eat('-') ->
                Delegate(parseFactorOp(context, blockContext), langContext::neg)

            eatAndExpectNot('!', '='::equals) ->
                OpNot(parseExpressionOp(context, blockContext = blockContext), langContext::isFalse)

            eat('~') -> {
                // reverse bits
                val expr = parseExpressionOp(context, blockContext = blockContext)
                Expression {
                    langContext.toNumber(expr(it)).toLong().inv()
                }
            }

            eat('(') -> {
                val exprs = buildList {
                    if (eat(')')) {
                        return@buildList
                    }
                    do {
                        add(parseAssignment(context, blockContext = blockContext))
                    } while (eat(','))

                    if (!eat(')')) {
                        throw SyntaxError("Missing ')'")
                    }
                }

                // arrow func
                if (eatSequence("=>")) {
                    OpConstant(parseArrowFunction(exprs, blockContext))
                } else {
                    exprs.getOrElse(0) {
                        throw SyntaxError("Unexpected token ')'")
                    }
                }
            }

            nextCharIs { it.isDigit() || it == '.' } -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("making const number... ")
                }
                var numberFormat = NumberFormat.Dec
                var isFloat = nextCharIs { it == '.' }
                val startPos = pos
                do {
                    nextChar()
                    when (ch.lowercaseChar()) {
                        '.' -> {
                            if (isFloat) {
                                break
                            }
                            isFloat = true
                        }

                        NumberFormat.Hex.prefix -> {
                            syntaxCheck(numberFormat == NumberFormat.Dec && !isFloat) {
                                "Invalid number at pos $startPos"
                            }
                            numberFormat = NumberFormat.Hex
                        }

                        NumberFormat.Oct.prefix -> {
                            syntaxCheck(numberFormat == NumberFormat.Dec && !isFloat) {
                                "Invalid number at pos $startPos"
                            }
                            numberFormat = NumberFormat.Oct
                        }

                        NumberFormat.Bin.prefix -> {
                            if (numberFormat == NumberFormat.Hex) {
                                continue
                            }
                            syntaxCheck(numberFormat == NumberFormat.Dec && !isFloat) {
                                "Invalid number at pos $startPos"
                            }
                            numberFormat = NumberFormat.Bin
                        }
                    }
                } while (ch.lowercaseChar().let {
                        it in numberFormat.alphabet || it in NumberFormatIndicators
                    })
                val num = try {
                    expr.substring(startPos, pos).let {
                        if (it.endsWith('.')) {
                            prevChar()
                            isFloat = false
                        }
                        if (isFloat) {
                            it.toDouble()
                        } else {
                            it.trimEnd('.')
                                .let { n -> numberFormat.prefix?.let(n::substringAfter) ?: n }
                                .toULong(numberFormat.radix)
                                .toLong()
                        }
                    }
                } catch (t: NumberFormatException) {
                    throw SyntaxError("Unexpected token ${expr[startPos]} at $startPos")
                }
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println(num)
                }
                OpConstant(langContext.fromKotlin(num))
            }

            nextCharIs('\''::equals) || nextCharIs('"'::equals) -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("making const string... ")
                }
                val c = ch
                val startPos = pos
                do {
                    nextChar()
                } while (!nextCharIs(c::equals) && pos < expr.length)
                syntaxCheck(eat(c)){
                    "Invalid string at pos $startPos"
                }
                val str = expr.substring(startPos, pos).drop(1).dropLast(1)
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println(str)
                }
                OpConstant(langContext.fromKotlin(str))
            }

            context !== globalContext && eat('[') -> { // index
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making index... ")
                }
                OpIndex(
                    variable = context,
                    index = parseExpressionOp(globalContext, blockContext = blockContext)
                ).also {
                    syntaxCheck(eat(']')) {
                        "Missing ']'"
                    }
                }
            }

            eat('[') -> { // make array
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making array... ")
                }
                val arrayArgs = buildList {
                    do {
                        if (eat(']')) { // empty list
                            return@buildList
                        }
                        add(parseExpressionOp(context, blockContext = blockContext))
                    } while (eat(','))
                    syntaxCheck(eat(']')) {
                        "Missing ]"
                    }
                }
                OpMakeArray(arrayArgs)
            }

            ch.isFun() -> {

                val startPos = pos
                do {
                    nextChar()
                } while (
                    pos < expr.length && ch.isFun() && !(isReserved(
                        expr.substring(
                            startPos,
                            pos
                        )
                    ) && ch == ' ')
                )

                val func = expr.substring(startPos, pos).trim()

                parseFunction(context, func, blockContext)
            }

            else -> throw SyntaxError("Unexpected token $ch at pos $pos")
        }

        return parsedOp.finish(blockContext, allowContinueWithContext)
    }

    private fun Expression.finish(blockContext: List<BlockContext>, allowContinueWithContext: Boolean): Expression {
        return when {
            !allowContinueWithContext -> this
            // inplace function invocation
            this is InterpretationContext && nextCharIs { it == '(' } -> {
                parseFunction(
                    context = this,
                    func = null,
                    blockContext = blockContext
                )
            }
            // begin condition || property || index
            eat('.')
                    || nextCharIs('['::equals) ->
                parseFactorOp(this, blockContext) // continue with receiver

            eatSequence("instanceof") -> {
                val obj = parseFactorOp(globalContext, emptyList())

                Expression {
                    val o = obj(it)
                    if (o !is ESObject) {
                        throw TypeError("Right-hand side of 'instanceof' is not an object ($obj)")
                    }
                    val thisObj = this(it)
                    if (thisObj is ESClass){
                        thisObj.instanceOf(o, it)
                    } else {
                        false
                    }
                }
            }
            else -> this
        }
    }

    private fun parseFunctionArgs(name: String?): List<Expression>? {

        if (!nextCharIs('('::equals)) {
            return null
        }

        val start = pos
        return buildList {
            when {
                eat('(') -> {
                    if (eat(')')) {
                        return@buildList //empty args
                    }
                    do {
                        add(parseAssignment(globalContext, emptyList()))
                    } while (eat(','))

                    if (!eat(')')) {
                        throw SyntaxError("Invalid or unexpected token at pos $start")
                    }
                }
            }
        }
    }


    private fun parseFunction(
        context: Expression,
        func: String?,
        blockContext: List<BlockContext>
    ): Expression {

        if (blockContext.lastOrNull() == BlockContext.Class){
            if (func == "static") {
                if (EXPR_DEBUG_PRINT_ENABLED){
                    println("parsing static class member")
                }
                val v = parseAssignment(globalContext, blockContext = blockContext)
                if (v is OpAssign){
                    v.isStatic = true
                }
                if (v is OpConstant && v.value is Function){
                    v.value.isStatic = true
                }
                return v
            }
        }

        return when (func) {
            "var", "let", "const" -> parseVariable(func)
            "typeof" -> parseTypeof()
            "null" -> OpConstant(null)
            "true" -> OpConstant(true)
            "false" -> OpConstant(false)
            "function" -> {
                OpConstant(parseFunctionDefinition(blockContext = blockContext))
            }
            "new" -> {
                if (EXPR_DEBUG_PRINT_ENABLED){
                    println("parsing 'new' class instantiation")
                }
                val decl = parseFactorOp(globalContext, emptyList(), allowContinueWithContext = false)
                syntaxCheck(decl is OpFunctionExec) {
                    "$decl is not a constructor"
                }
                ESClassInstantiation(decl.name, decl.parameters)
            }
            "class" -> {
                OpConstant(parseClassDefinition())
            }

            "for" ->  parseForLoop(blockContext)

            "while" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making while loop")
                }

                OpWhileLoop(
                    condition = parseWhileCondition(),
                    body = parseBlock(blockContext = blockContext + BlockContext.Loop),
                    isFalse = langContext::isFalse
                )
            }

            "do" -> parseDoWhile(blockContext)
            "if" -> parseIf(blockContext)
            "continue" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("parsing loop continue")
                }

                syntaxCheck(BlockContext.Loop in blockContext){
                    "Illegal continue statement: no surrounding iteration statement"
                }

                OpContinue()
            }

            "break" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("parsing loop break")
                }
                syntaxCheck(BlockContext.Loop in blockContext){
                    "Illegal break statement"
                }
                OpBreak()
            }

            "return" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making return")
                }
                syntaxCheck(BlockContext.Function in blockContext) {
                    "Illegal return statement"
                }
                val expr = parseExpressionOp(
                    context = globalContext,
                    blockContext = blockContext,
                )
                OpReturn(expr)
            }
            "throw" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("parsing throw")
                }
                val throwable = parseAssignment(globalContext, blockContext)

                Expression {
                    val t = throwable(it)
                    throw if (t is Throwable) t else ThrowableValue(t)
                }
            }

            "try" -> parseTryCatch(blockContext)

            else -> {
                val args = parseFunctionArgs(func)

                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making fun $func")
                }

                return when (context) {
                    is InterpretationContext -> {
                        val f = context.interpret(func, args)
                        when {
                            f != null -> f
                            func != null && blockContext.lastOrNull() == BlockContext.Class -> {
                                if (args != null) {
                                    if (EXPR_DEBUG_PRINT_ENABLED) {
                                        println("Parsing class method...")
                                    }
                                    OpConstant(
                                        parseFunctionDefinition(
                                            name = func,
                                            args = args,
                                            blockContext = emptyList()
                                        )
                                    )
                                } else {
                                    parseAssignment(
                                        context = globalContext,
                                        blockContext = blockContext.dropLastWhile { it == BlockContext.Class },
                                        variableName = func
                                    )
                                }
                            }
                            args != null && func != null -> {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("parsed call for defined function $func")
                                }
                                OpFunctionExec(func, null, args)
                            }

                            args == null && func != null -> {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("making GetVariable $func...")
                                }
                                OpGetVariable(name = func, receiver = null)
                            }

                            else -> unresolvedReference(
                                ref = func ?: "null",
                                obj = context::class.simpleName
                                    ?.substringAfter("Op")
                                    ?.substringBefore("Context")
                            )
                        }
                    }
                    else -> {
                        when {
                            args != null && func != null -> {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("parsed call for function $func with receiver $context")
                                }
                                OpFunctionExec(
                                    name = func,
                                    receiver = context,
                                    parameters = args
                                )
                            }

                            args == null && func != null -> {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("making GetVariable $func with receiver $context... ")
                                }
                                return OpGetVariable(name = func, receiver = context)
                            }

                            else -> unresolvedReference(func ?: "null")
                        }
                    }
                }
            }
        }
    }

    private fun parseVariable(type: String) :Expression {
        val scope = when (type) {
            "var" -> VariableType.Global
            "let" -> VariableType.Local
            else -> VariableType.Const
        }

        val start = pos

        return when (val expr = parseAssignment(globalContext, emptyList())) {
            is OpAssign -> {
                OpAssign(
                    type = scope,
                    variableName = expr.variableName,
                    assignableValue = expr.assignableValue,
                    merge = null
                )
            }

            is OpGetVariable -> {
                OpAssign(
                    type = scope,
                    variableName = expr.name,
                    assignableValue = OpConstant(Unit),
                    merge = null
                )
            }

            else -> throw SyntaxError(
                "Unexpected identifier '${
                    this.expr.substring(start).substringBefore(' ').trim()
                }'"
            )
        }
    }

    private fun parseTypeof() : Expression {
        val isArg = eat('(')
        val expr = parseAssignment(
            context = globalContext,
            blockContext = emptyList(),
            unaryOnly = true
        )
        if (isArg) {
            syntaxCheck(eat(')')) {
                "Missing )"
            }
        }
        return Expression {
            when (val v = expr(it)) {
                null -> "object"
                Unit -> "undefined"
                true, false -> "boolean"

                is ESAny -> v.type
                is Callable -> "function"
                else -> v::class.simpleName
            }
        }
    }

    private fun parseDoWhile(blockContext: List<BlockContext>) : Expression {
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("making do/while loop")
        }

        val body = parseBlock(blockContext = blockContext + BlockContext.Loop)

        syntaxCheck(body is OpBlock) {
            "Invalid do/while syntax"
        }

        syntaxCheck(eatSequence("while")) {
            "Missing while condition in do/while block"
        }
        val condition = parseWhileCondition()

        return OpDoWhileLoop(
            condition = condition,
            body = body,
            isFalse = langContext::isFalse
        )
    }
    private fun parseIf(blockContext: List<BlockContext>) : Expression {
        if (EXPR_DEBUG_PRINT_ENABLED) {
            print("parsing if...")
        }

        val condition = parseExpressionOp(
            context = globalContext,
            blockContext = blockContext,
        )

        val onTrue = parseBlock(blockContext = blockContext)

        val onFalse = if (eatSequence("else")) {
            parseBlock(blockContext = blockContext)
        } else null

        return OpIfCondition(
            condition = condition,
            onTrue = onTrue,
            onFalse = onFalse
        )
    }

    private fun parseWhileCondition(): Expression {
        syntaxCheck(eat('(')) {
            "Missing while loop condition"
        }

        val condition = parseExpressionOp(globalContext, blockContext = emptyList(),)

        syntaxCheck(eat(')')) {
            "Missing closing ')' in loop condition"
        }
        return condition
    }

    private fun parseTryCatch(blockContext: List<BlockContext>): Expression {
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("making try")
        }
        val tryBlock = parseBlock(requireBlock = true, blockContext = blockContext)
        val catchBlock = if (eatSequence("catch")) {

            if (eat('(')) {
                val start = pos
                val arg = parseFactorOp(globalContext, emptyList())
                syntaxCheck(arg is OpGetVariable){
                    "Invalid syntax at $start"
                }
                syntaxCheck(eat(')')){
                    "Invalid syntax at $pos"
                }
                arg.name to parseBlock(
                    scoped = false,
                    requireBlock = true,
                    blockContext = blockContext
                )
            } else {
                null to parseBlock(requireBlock = true, blockContext = blockContext)
            }
        } else null

        val finallyBlock = if (eatSequence("finally")) {
            parseBlock(requireBlock = true, blockContext = blockContext)
        } else null

        return OpTryCatch(
            tryBlock = tryBlock,
            catchVariableName = catchBlock?.first,
            catchBlock = catchBlock?.second,
            finallyBlock = finallyBlock
        )
    }

    private fun parseForLoop(parentBlockContext: List<BlockContext>): Expression {

        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("making for loop")
        }

        syntaxCheck(eat('(')) {
            "Invalid for loop"
        }

        val assign = if (eat(';')) null
        else parseAssignment(
            context = globalContext,
            blockContext = emptyList(),
        )
        syntaxCheck(assign is OpAssign?) {
            "Invalid for loop"
        }
        if (assign != null) {
            syntaxCheck(eat(';')) {
                "Invalid for loop"
            }
        }
        val comparison = if (eat(';')) null
        else parseAssignment(
            context = globalContext,
            blockContext = emptyList(),
        )
        if (comparison != null) {
            syntaxCheck(eat(';')) {
                "Invalid for loop"
            }
        }
        val increment = if (eat(')')) null
        else parseAssignment(
            context = globalContext,
            blockContext = emptyList(),
        )

        if (increment != null) {
            syntaxCheck(eat(')')) {
                "Invalid for loop"
            }
        }

        val body = parseBlock(blockContext = parentBlockContext + BlockContext.Loop)

        return OpForLoop(
            assignment = assign,
            increment = increment,
            comparison = comparison,
            isFalse = langContext::isFalse,
            body = body
        )
    }

    private fun parseArrowFunction(
        args: List<Expression>,
        blockContext: List<BlockContext>
    ): Function {
        val fArgs = args.filterIsInstance<OpGetVariable>()
        syntaxCheck(fArgs.size == args.size) {
            "Invalid arrow function"
        }
        val lambda = parseBlock(blockContext = blockContext + BlockContext.Function)

        return Function(
            "",
            fArgs.map { FunctionParam(it.name) },
            body = lambda
        )
    }

    private fun parseClassDefinition() : ESClass {
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("parsing class...")
        }

        val name = parseFactorOp(globalContext, emptyList())

        syntaxCheck(name is OpGetVariable) {
            "Invalid class declaration"
        }

        val extends = if (eatSequence("extends")) {
            parseFactorOp(globalContext, emptyList())
        } else null

        val static = mutableListOf<StaticClassMember>()

        val body = parseBlock(
            scoped = false,
            requireBlock = true,
            blockContext = listOf(BlockContext.Class),
            static = static
        ) as OpBlock

        val functions = body.expressions
            .filterIsInstance<OpConstant>()
            .map { it.value }
            .filterIsInstance<Function>()

        syntaxCheck(functions.size == body.expressions.size) {
            "Invalid class body (${name.name})"
        }

        val constructors = functions.filter { it.name == "constructor" }.toSet()

        syntaxCheck(constructors.size <= 1) {
            "A class may only have one constructor"
        }

        val clazz = ESClassBase(
            name = name.name,
            functions = functions - constructors,
            construct = constructors.singleOrNull(),
            extends = extends,
            static = static
        )

        return clazz
    }

    private fun parseFunctionDefinition(
        name: String? = null,
        args: List<Expression>? = null,
        blockContext: List<BlockContext>
    ): Function {

        val start = pos

        val actualName = name ?: run {

            while (ch != '(') {
                nextChar()
            }

            expr.substring(start, pos).trim()
        }
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("making defined function $actualName")
        }

        val nArgs = (args ?: parseFunctionArgs(actualName))?.let { a ->
            a.map {
                when (it) {
                    is OpGetVariable -> FunctionParam(name = it.name, default = null)
                    is OpAssign -> FunctionParam(
                        name = it.variableName,
                        default = it.assignableValue
                    )

                    else -> throw SyntaxError("Invalid function declaration at $start")
                }
            }
        }

        syntaxCheck(nArgs != null) {
            "Missing function args"
        }

        syntaxCheck(nextCharIs('{'::equals)) {
            "Missing function body at $pos"
        }


        val block = parseBlock(
            scoped = false,
            blockContext = blockContext + BlockContext.Function
        )

        return Function(
            name = actualName,
            parameters = nArgs,
            body = block,
            isClassMember =  args != null
        )
    }

    private fun parseObject(extraFields: Map<String, Expression> = emptyMap()): Expression {
        val props = buildMap {
            while (!eat('}')) {

                val start = pos
                val name = parseTermOp(globalContext, emptyList())
                syntaxCheck(name is OpGetVariable) {
                    "Invalid syntax at $start"
                }

                syntaxCheck(eat(':')) {
                    "Invalid syntax at $pos"
                }

                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making object property ${name.name}")
                }

                this[name.name] = parseExpressionOp(globalContext, null, emptyList())
                eat(',')
            }
        } + extraFields
        return Expression { r ->
            Object("") {
                props.forEach {
                    it.key eq it.value.invoke(r)
                }
            }
        }
    }

    private fun parseBlock(
        scoped: Boolean = true,
        requireBlock: Boolean = false,
        blockContext: List<BlockContext>,
        static : MutableList<StaticClassMember>? = null,
    ): Expression {
        var funcIndex = 0
        val list = buildList {
            if (eat('{')) {
                while (!eat('}') && pos < expr.length) {
                    val expr = parseAssignment(globalContext, blockContext, isExpressionStart = true)

                    if (size == 0 && expr is OpGetVariable && eat(':')) {
                        return parseObject(
                            mapOf(
                                expr.name to parseExpressionOp(
                                    globalContext,
                                    null,
                                    emptyList()
                                )
                            )
                        )
                    }

                    when {
                        expr is OpAssign && expr.isStatic -> {
                            static?.add(StaticClassMember.Variable(expr.variableName, expr.assignableValue))
                        }
                        expr is OpConstant && expr.value is Function && expr.value.isStatic -> {
                            static?.add(StaticClassMember.Method(expr.value))
                        }
                        expr is OpConstant && (expr.value is Function && !expr.value.isClassMember || expr.value is ESClass) -> {
                            val name = (expr.value as Named).name

                            if (EXPR_DEBUG_PRINT_ENABLED){
                                println("registering '$name' as class or top level function")
                            }
                            add(
                                index = funcIndex++,
                                element = OpAssign(
                                    type = VariableType.Local,
                                    variableName = name,
                                    receiver = null,
                                    assignableValue = expr,
                                    merge = null
                                )
                            )
                            if (expr.value is ESClass) {
                                expr.value.static.forEach { s ->
                                    add(
                                        index = funcIndex++,
                                        element = Expression {
                                            s.assignTo(expr.value, it)
                                        }
                                    )
                                }
                            }
                        }
                        else -> add(expr)
                    }
                    eat(';')
                    eat(';')
                }
            } else {
                if (requireBlock) {
                    throw SyntaxError("Unexpected token at $pos: block start was expected")
                }
                add(parseAssignment(globalContext, blockContext))
            }
        }
        return OpBlock(list, scoped)
    }
}


@OptIn(ExperimentalContracts::class)
internal fun checkArgsNotNull(args : List<*>?, func : String) {
    contract {
        returns() implies (args != null)
    }
    checkNotNull(args){
        "$func call was missing"
    }
}

@OptIn(ExperimentalContracts::class)
internal fun checkArgs(args : List<*>?, count : Int, func : String) {
    contract {
        returns() implies (args != null)
    }
    checkNotNull(args){
        "$func call was missing"
    }
    require(args.size == count){
        "$func takes $count arguments, but ${args.size} got"
    }
}

private val funMap = (('a'..'z').toList() + ('A'..'Z').toList() + '$' + '_' ).associateBy { it }

private fun Char.isFun() = isDigit() || funMap[this] != null


private enum class NumberFormat(
    val radix : Int,
    val alphabet : String,
    val prefix : Char?
) {
    Dec(10, ".0123456789", null),
    Hex(16, "0123456789abcdef", 'x'),
    Oct(8, "01234567", 'o'),
    Bin(2, "01", 'b')
}

@OptIn(ExperimentalContracts::class)
public inline fun syntaxCheck(value: Boolean, lazyMessage: () -> Any) {
    contract {
        returns() implies value
    }

    if (!value) {
        val message = lazyMessage()
        throw SyntaxError(message.toString())
    }
}

private val NumberFormatIndicators = NumberFormat.entries.mapNotNull { it.prefix }

private val reservedKeywords = setOf(
    "function","return","do","while","for"
)

private fun isReserved(keyword : String) = keyword in reservedKeywords
