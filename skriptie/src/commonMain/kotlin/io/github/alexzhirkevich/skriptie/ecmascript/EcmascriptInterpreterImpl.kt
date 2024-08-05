package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.InterpretationContext
import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.VariableType
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


internal class EcmascriptInterpreterImpl(
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

    private fun parseAssignment(context: Expression, blockContext: List<BlockContext>): Expression {
        var x = parseExpressionOp(context, blockContext = blockContext)
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("Parsing assignment for $x")
        }
        while (true) {
            prepareNextChar()
            x = when {
                eatSequence("+=") -> parseAssignmentValue(x, langContext::sum)
                eatSequence("-=") -> parseAssignmentValue(x, langContext::sub)
                eatSequence("*=") -> parseAssignmentValue(x, langContext::mul)
                eatSequence("/=") -> parseAssignmentValue(x, langContext::div)
                eatSequence("%=") -> parseAssignmentValue(x, langContext::mod)
                eat('=') -> parseAssignmentValue(x, null)
                eatSequence("++") -> {
                    check(x.isAssignable()) {
                        "Not assignable"
                    }
                    OpIncDecAssign(
                        variable = x,
                        preAssign = false,
                        op = langContext::inc
                    )
                }

                eatSequence("--") -> {
                    check(x.isAssignable()) {
                        "Not assignable"
                    }
                    OpIncDecAssign(
                        variable = x,
                        preAssign = false,
                        op = langContext::dec
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
                assignableValue = parseAssignment(globalContext, emptyList()),
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
        blockContext: List<BlockContext>
    ): Expression {
        var x = parseTermOp(context, blockContext)
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

    private fun parseTermOp(context: Expression, blockContext: List<BlockContext>): Expression {
        var x = parseFactorOp(context, blockContext)
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

    private fun parseFactorOp(context: Expression, blockContext: List<BlockContext>): Expression {
        val parsedOp = when {

            nextCharIs('{'::equals) -> parseBlock(context = emptyList())

            context === globalContext && eatSequence("++") -> {
                val start = pos
                val variable = parseFactorOp(globalContext, blockContext)
                require(variable.isAssignable()) {
                    "Unexpected '++' as $start"
                }
                OpIncDecAssign(
                    variable = variable,
                    preAssign = true,
                    op = langContext::inc
                )
            }

            context === globalContext && eatSequence("--") -> {
                val start = pos
                val variable = parseFactorOp(globalContext, blockContext)
                require(variable.isAssignable()) {
                    "Unexpected '--' as $start"
                }
                OpIncDecAssign(
                    variable = variable,
                    preAssign = true,
                    op = langContext::dec
                )
            }

            context === globalContext && eat('+') ->
                Delegate(parseFactorOp(context, blockContext), langContext::pos)

            context === globalContext && eat('-') ->
                Delegate(parseFactorOp(context, blockContext), langContext::neg)

            context === globalContext && !nextSequenceIs("!=") && eat('!') ->
                OpNot(parseExpressionOp(context, blockContext = blockContext), langContext::isFalse)

            context === globalContext && eat('(') -> {
                parseExpressionOp(context, blockContext = blockContext).also {
                    require(eat(')')) {
                        "Bad expression: Missing ')'"
                    }
                }
            }

            context === globalContext && nextCharIs { it.isDigit() || it == '.' } -> {
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
                            check(numberFormat == NumberFormat.Dec && !isFloat) {
                                "Invalid number at pos $startPos"
                            }
                            numberFormat = NumberFormat.Hex
                        }

                        NumberFormat.Oct.prefix -> {
                            check(numberFormat == NumberFormat.Dec && !isFloat) {
                                "Invalid number at pos $startPos"
                            }
                            numberFormat = NumberFormat.Oct
                        }

                        NumberFormat.Bin.prefix -> {
                            if (numberFormat == NumberFormat.Hex) {
                                continue
                            }
                            check(numberFormat == NumberFormat.Dec && !isFloat) {
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

            context === globalContext && nextCharIs('\''::equals) || nextCharIs('"'::equals) -> {
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
                    context,
                    parseExpressionOp(globalContext, blockContext = blockContext)
                ).also {
                    require(eat(']')) {
                        "Bad expression: Missing ']'"
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
                    require(eat(']')) {
                        "Bad expression: missing ]"
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
            "var", "let", "const" -> {
                val scope = when (func) {
                    "var" -> VariableType.Global
                    "let" -> VariableType.Local
                    else -> VariableType.Const
                }

                val start = pos

                when (val expr = parseAssignment(globalContext, emptyList())) {
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


            "null" -> OpConstant(null)
            "true" -> OpConstant(true)
            "false" -> OpConstant(false)
            "function" -> {
                OpConstant(parseFunctionDefinition(blockContext = blockContext))
            }

            "for" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making for loop")
                }
                parseForLoop(blockContext)
            }

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

            "do" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making do/while loop")
                }

                val body = parseBlock(context = blockContext + BlockContext.Loop)

                check(body is OpBlock) {
                    "Invalid do/while syntax"
                }

                check(eatSequence("while")) {
                    "Missing while condition in do/while block"
                }
                val condition = parseWhileCondition()

                OpDoWhileLoop(
                    condition = condition,
                    body = body,
                    isFalse = langContext::isFalse
                )
            }

            "if" -> {

                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("parsing if...")
                }

                val condition = parseExpressionOp(globalContext, blockContext = blockContext)

                val onTrue = parseBlock(context = blockContext)

                val onFalse = if (eatSequence("else")) {
                    parseBlock(context = blockContext)
                } else null

                OpIfCondition(
                    condition = condition,
                    onTrue = onTrue,
                    onFalse = onFalse
                )
            }

            "continue" -> {
                if (BlockContext.Loop in blockContext) {
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("parsing loop continue")
                    }
                    OpContinue()
                } else {
                    throw SyntaxError("Illegal continue statement: no surrounding iteration statement")
                }
            }

            "break" -> {
                if (BlockContext.Loop in blockContext) {
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("parsing loop break")
                    }
                    OpBreak()
                } else {
                    throw SyntaxError("Illegal break statement")
                }
            }

            "return" -> {
                if (BlockContext.Function in blockContext) {
                    val expr = parseExpressionOp(globalContext, blockContext = blockContext)
                    if (EXPR_DEBUG_PRINT_ENABLED) {
                        println("making return with $expr")
                    }
                    OpReturn(expr)
                } else {
                    throw SyntaxError("Illegal return statement")
                }
            }

            "try" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making try $expr")
                }
                parseTryCatch(blockContext)
            }

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

    private fun parseWhileCondition(): Expression {
        check(eat('(')) {
            "Missing while loop condition"
        }

        val condition = parseExpressionOp(globalContext, blockContext = emptyList())

        check(eat(')')) {
            "Missing closing ')' in loop condition"
        }
        return condition
    }

    private fun parseTryCatch(blockContext: List<BlockContext>): Expression {
        val tryBlock = parseBlock(requireBlock = true, context = blockContext)
        val catchBlock = if (eatSequence("catch")) {

            if (eat('(')) {
                val start = pos
                while (!eat(')') && pos < expr.length) {
                    //nothing
                }
                expr.substring(start, pos).trim() to parseBlock(
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
        check(eat('('))

        val assign = if (eat(';')) null else parseAssignment(globalContext, emptyList())
        check(assign is OpAssign?)
        if (assign != null) {
            check(eat(';'))
        }
        println(assign)
        val comparison = if (eat(';')) null else parseAssignment(globalContext, emptyList())
        if (comparison != null) {
            check(eat(';'))
        }
        println(comparison)
        val increment = if (eat(')')) null else parseAssignment(globalContext, emptyList())

        println(increment)
        if (increment != null) {
            check(eat(')'))
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

                    else -> error("Invalid function declaration at $start")
                }
            }
        }

        checkNotNull(args) {
            "Missing function args"
        }


        check(nextCharIs('{'::equals)) {
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

    private fun parseBlock(
        scoped: Boolean = true,
        requireBlock: Boolean = false,
        context: List<BlockContext>
    ): Expression {
        var funcIndex = 0
        val list = buildList {
            if (eat('{')) {
                while (!eat('}') && pos < expr.length) {
                    val expr = parseAssignment(globalContext, context)

                    if (expr is OpConstant && expr.value is Function) {
                        add(funcIndex++, OpAssign(VariableType.Local, expr.value.name, expr, null))
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

private val NumberFormatIndicators = NumberFormat.entries.mapNotNull { it.prefix }

private val reservedKeywords = setOf(
    "function","return","do","while","for"
)

private fun isReserved(keyword : String) = keyword in reservedKeywords
