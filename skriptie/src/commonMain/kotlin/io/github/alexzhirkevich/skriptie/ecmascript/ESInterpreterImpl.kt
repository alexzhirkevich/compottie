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
import io.github.alexzhirkevich.skriptie.common.OpAssign
import io.github.alexzhirkevich.skriptie.common.OpAssignByIndex
import io.github.alexzhirkevich.skriptie.common.OpBlock
import io.github.alexzhirkevich.skriptie.common.OpBoolean
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
import io.github.alexzhirkevich.skriptie.common.OpMakeArray
import io.github.alexzhirkevich.skriptie.common.OpNot
import io.github.alexzhirkevich.skriptie.common.OpReturn
import io.github.alexzhirkevich.skriptie.common.OpTryCatch
import io.github.alexzhirkevich.skriptie.common.OpWhileLoop
import io.github.alexzhirkevich.skriptie.common.SyntaxError
import io.github.alexzhirkevich.skriptie.common.ThrowableValue
import io.github.alexzhirkevich.skriptie.common.unresolvedReference
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.isAssignable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal val EXPR_DEBUG_PRINT_ENABLED = false
internal enum class LogicalContext {
    And, Or, Compare
}

internal enum class BlockContext {
    None, Loop, Function
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
        val block = parseBlock(scoped = false, context = emptyList())
        return Script { langContext.toKotlin(block(it)) }
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
        isExpressionStart: Boolean = false
    ): Expression {
        var x = parseExpressionOp(
            context,
            blockContext = blockContext,
            isExpressionStart = isExpressionStart
        )
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("Parsing assignment for $x")
        }

        val checkAssignment = {
            if (unaryOnly)
                throw SyntaxError("Invalid left-hand side in assignment")
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

                eatSequence("=>") -> OpConstant(parseArrowFunction(listOf(x), blockContext))
                eat('=') -> {
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

                    val onTrue = parseAssignment(globalContext, blockContext)

                    if (!eat(':')) {
                        throw SyntaxError("Unexpected end of input")
                    }
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("making ternary operator: onFalse...")
                    }
                    val onFalse = parseAssignment(globalContext, blockContext)

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
        isExpressionStart: Boolean = false
    ): Expression {
        var x = parseTermOp(context, blockContext, isExpressionStart)
        while (true) {
            prepareNextChar()
            x = when {
                logicalContext != LogicalContext.Compare && eatSequence("&&") ->
                    OpBoolean(
                        parseExpressionOp(globalContext, LogicalContext.And, blockContext),
                        x,
                        langContext::isFalse, Boolean::and
                    )

                logicalContext == null && eatSequence("||") ->
                    OpBoolean(
                        parseExpressionOp(globalContext, LogicalContext.Or, blockContext),
                        x,
                        langContext::isFalse, Boolean::or
                    )

                eatSequence("<=") -> OpCompare(
                    x,
                    parseExpressionOp(globalContext, LogicalContext.Compare, blockContext)
                ) { a, b ->
                    OpLessComparator(a, b) || OpEqualsComparator(a, b)
                }

                eatSequence("<") -> OpCompare(
                    x,
                    parseExpressionOp(globalContext, LogicalContext.Compare, blockContext),
                    OpLessComparator
                )

                eatSequence(">=") -> OpCompare(
                    x,
                    parseExpressionOp(globalContext, LogicalContext.Compare, blockContext)
                ) { a, b ->
                    OpGreaterComparator(a, b) || OpEqualsComparator(a, b)
                }

                eatSequence(">") -> OpCompare(
                    x,
                    parseExpressionOp(globalContext, LogicalContext.Compare, blockContext),
                    OpGreaterComparator
                )

                eatSequence("===") -> OpEquals(
                    x,
                    parseExpressionOp(globalContext, LogicalContext.Compare, blockContext),
                    true
                )

                eatSequence("==") -> OpEquals(
                    x,
                    parseExpressionOp(globalContext, LogicalContext.Compare, blockContext),
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
                        parseExpressionOp(globalContext, LogicalContext.Compare, blockContext),
                        true
                    ),
                    langContext::isFalse
                )

                !nextSequenceIs("++") && !nextSequenceIs("+=") && eat('+') ->
                    Delegate(x, parseTermOp(globalContext, blockContext), langContext::sum)

                !nextSequenceIs("--") && !nextSequenceIs("-=") && eat('-') ->
                    Delegate(x, parseTermOp(globalContext, blockContext), langContext::sub)

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
                !nextSequenceIs("*=") && eat('*') -> Delegate(
                    x,
                    parseFactorOp(globalContext, blockContext),
                    langContext::mul
                )

                !nextSequenceIs("/=") && eat('/') -> Delegate(
                    x,
                    parseFactorOp(globalContext, blockContext),
                    langContext::div
                )

                !nextSequenceIs("%=") && eat('%') -> Delegate(
                    x,
                    parseFactorOp(globalContext, blockContext),
                    langContext::mod
                )

                else -> return x
            }
        }
    }

    private fun parseFactorOp(
        context: Expression,
        blockContext: List<BlockContext>,
        isExpressionStart: Boolean = false
    ): Expression {
        val parsedOp = when {

            isExpressionStart && nextCharIs('{'::equals) ->
                parseBlock(context = emptyList())

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

            !nextSequenceIs("!=") && eat('!') ->
                OpNot(parseExpressionOp(context, blockContext = blockContext), langContext::isFalse)

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
                } while (!eat(c))
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

        return parsedOp.finish(blockContext)
    }

    private fun Expression.finish(blockContext: List<BlockContext>): Expression {
        return when {
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

        return when (func) {
            "var", "let", "const" -> parseVariable(func)
            "typeof" -> parseTypeof()
            "null" -> OpConstant(null)
            "true" -> OpConstant(true)
            "false" -> OpConstant(false)
            "function" -> {
                OpConstant(parseFunctionDefinition(blockContext = blockContext))
            }

            "for" ->  parseForLoop(blockContext)

            "while" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making while loop")
                }

                OpWhileLoop(
                    condition = parseWhileCondition(),
                    body = parseBlock(context = blockContext + BlockContext.Loop),
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
                    is InterpretationContext -> context.interpret(func, args)
                        ?: run {
                            if (args != null && func != null) {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("parsed call for defined function $func")
                                }
                                OpFunctionExec(func, null, args)
                            } else null
                        }
                        ?: run {
                            if (args == null && func != null) {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("making GetVariable $func...")
                                }
                                OpGetVariable(name = func, receiver = null)
                            } else {
                                null
                            }
                        }
                        ?: unresolvedReference(
                            ref = func ?: "null",
                            obj = context::class.simpleName
                                ?.substringAfter("Op")
                                ?.substringBefore("Context")
                        )

                    else -> {
                        kotlin.run {
                            if (args != null && func != null) {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("parsed call for function $func with receiver $context")
                                }
                                return@run OpFunctionExec(
                                    name = func,
                                    receiver = context,
                                    parameters = args
                                )
                            }
                            if (args == null && func != null) {
                                if (EXPR_DEBUG_PRINT_ENABLED) {
                                    println("making GetVariable $func with receiver $context... ")
                                }
                                return@run OpGetVariable(name = func, receiver = context)
                            }
                            unresolvedReference(func ?: "null")
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

        val body = parseBlock(context = blockContext + BlockContext.Loop)

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

        val onTrue = parseBlock(context = blockContext)

        val onFalse = if (eatSequence("else")) {
            parseBlock(context = blockContext)
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
        val tryBlock = parseBlock(requireBlock = true, context = blockContext)
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
                    context = blockContext
                )
            } else {
                null to parseBlock(requireBlock = true, context = blockContext)
            }
        } else null

        val finallyBlock = if (eatSequence("finally")) {
            parseBlock(requireBlock = true, context = blockContext)
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

        val body = parseBlock(scoped = false, context = parentBlockContext + BlockContext.Loop)

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
        val lambda = parseBlock(context = blockContext + BlockContext.Function)

        return Function(
            "",
            fArgs.map { FunctionParam(it.name) },
            body = lambda
        )
    }

    private fun parseFunctionDefinition(
        name: String? = null,
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

        val args = parseFunctionArgs(actualName).let { args ->
            args?.map {
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

        if (args == null) {
            throw SyntaxError("Missing function args")
        }

        syntaxCheck(nextCharIs('{'::equals)) {
            "Missing function body at $pos"
        }


        val block = parseBlock(
            scoped = false,
            context = blockContext + BlockContext.Function
        )

        return Function(
            name = actualName,
            parameters = args,
            body = block
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
        context: List<BlockContext>
    ): Expression {
        var funcIndex = 0
        val list = buildList {
            if (eat('{')) {
                while (!eat('}') && pos < expr.length) {
                    val expr = parseAssignment(globalContext, context, isExpressionStart = true)

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

                    if (expr is OpConstant && expr.value is Function) {
                        add(
                            funcIndex++,
                            OpAssign(
                                type = VariableType.Local,
                                variableName = expr.value.name,
                                receiver = null,
                                assignableValue = expr,
                                merge = null
                            )
                        )
                    } else {
                        add(expr)
                    }
                    eat(';')
                    eat(';')
                }
            } else {
                if (requireBlock) {
                    throw SyntaxError("Unexpected token at $pos: block start was expected")
                }
                add(parseAssignment(globalContext, context))
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
