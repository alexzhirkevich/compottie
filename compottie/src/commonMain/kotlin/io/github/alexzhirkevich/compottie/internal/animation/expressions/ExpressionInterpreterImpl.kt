package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.OpGlobalContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.JsContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.FunctionParam
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpBlock
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpBoolean
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpDoWhileLoop
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpEquals
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpFunction
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpFunctionExec
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpIfCondition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpNot
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpReturn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpTryCatch
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpWhileLoop
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpAdd
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpDecrement
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpDiv
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpIncrement
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMod
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpMul
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpSub
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpUnaryMinus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.OpUnaryPlus
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.isAssignable
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.unresolvedReference
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssign
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpAssignByIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpCompare
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpConstant
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpEqualsComparator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpGetVariable
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpGreaterComparator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpLessComparator
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpMakeArray
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpVar
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class ExpressionInterpreterImpl(
    private val expr : String,
    private val context: EvaluationContext
) : ExpressionInterpreter {

    private var pos = -1
    private var ch: Char = ' '

    override fun interpret(): Expression {
        val expressions = buildList {
            pos = -1
            ch = ' '
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("Parsing $expr")
            }
            nextChar()
            var x: Expression? = null
            do {
                while (eat(';')) {
                }
                if (pos >= expr.length) {
                    break
                }

                x = parseAssignment(if (x is ExpressionContext<*>) x else OpGlobalContext)
                add(x)
            } while (pos < expr.length)

            require(pos <= expr.length) {
                "Unexpected Lottie expression $expr"
            }
        }
        return OpBlock(expressions, false).also {
            pos = -1
            ch = ' '
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("Expression parsed: $expr")
            }
        }
    }

    private fun prepareNextChar(){
        while (ch.skip() && pos < expr.length){
            nextChar()
        }
    }

    private fun nextChar() {
        ch = if (++pos < expr.length) expr[pos] else ' '
    }

    private fun prevChar() {
        ch = if (--pos > 0 && pos < expr.length) expr[pos] else ' '
    }

    private fun Char.skip() : Boolean = this == ' ' || this == '\n'

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

    private fun eatSequence(seq : String): Boolean {

        val p = pos
        val c = ch

        if (seq.isEmpty())
            return true

        if (!eat(seq[0])) {
            return false
        }

        return if (expr.indexOf(seq, startIndex = pos-1) == pos-1){
            pos += seq.length -1
            ch = expr[pos.coerceIn(expr.indices)]
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
            println("Parsing assignment for $x")
        }
        while (true) {
            prepareNextChar()
            x = when {
                eatSequence("+=") -> parseAssignmentValue(x, ::OpAdd)
                eatSequence("-=") -> parseAssignmentValue(x, ::OpSub)
                eatSequence("*=") -> parseAssignmentValue(x, ::OpMul)
                eatSequence("/=") -> parseAssignmentValue(x, ::OpDiv)
                eatSequence("%=") -> parseAssignmentValue(x, ::OpMod)
                eat('=') -> parseAssignmentValue(x, null)
                x.isAssignable() && eatSequence("++") -> OpIncrement(x)
                x.isAssignable() && eatSequence("--") -> OpDecrement(x)
                else -> return x
            }
        }
    }

    private fun parseAssignmentValue(x : Expression, merge : ((Any, Any) -> Any)? = null) =  when {
        x is OpIndex && x.variable is OpGetVariable -> OpAssignByIndex(
            variableName = x.variable.name,
            scope = x.variable.assignmentType,
            index = x.index,
            assignableValue = parseAssignment(OpGlobalContext),
            merge = merge
        ).also {
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("parsing assignment with index for ${x.variable.name}")
            }
        }

        x is OpGetVariable -> OpAssign(
            variableName = x.name,
            assignableValue = parseAssignment(OpGlobalContext),
            type = x.assignmentType,
            merge = merge
        ).also {
            if (EXPR_DEBUG_PRINT_ENABLED) {
                println("parsing assignment for ${x.name} in ${it.type} scope")
            }
        }

        else -> error("Invalid assignment")
    }

    private fun parseExpressionOp(context: Expression, logicalContext: LogicalContext? = null): Expression {
        var x = parseTermOp(context)
        while (true) {
            prepareNextChar()
            x = when {
                logicalContext != LogicalContext.Compare && eatSequence("&&") ->
                    OpBoolean(parseExpressionOp(OpGlobalContext, LogicalContext.And),x,  Boolean::and)
                logicalContext == null && eatSequence("||") ->
                    OpBoolean(parseExpressionOp(OpGlobalContext, LogicalContext.Or),x,  Boolean::or)
                eatSequence("<=") -> OpCompare(x, parseExpressionOp(OpGlobalContext,  LogicalContext.Compare)) { a, b ->
                    OpLessComparator(a, b) || OpEqualsComparator(a, b)
                }
                eatSequence("<") -> OpCompare(x, parseExpressionOp(OpGlobalContext,  LogicalContext.Compare), OpLessComparator)
                eatSequence(">=") -> OpCompare(x, parseExpressionOp(OpGlobalContext,  LogicalContext.Compare)) { a, b ->
                    OpGreaterComparator(a, b) || OpEqualsComparator(a, b)
                }
                eatSequence(">") -> OpCompare(x, parseExpressionOp(OpGlobalContext, LogicalContext.Compare), OpGreaterComparator)
                eatSequence("===") -> OpEquals(x, parseExpressionOp(OpGlobalContext, LogicalContext.Compare), true)
                eatSequence("==") -> OpEquals(x, parseExpressionOp(OpGlobalContext, LogicalContext.Compare), false)
                eatSequence("!==") -> OpNot(OpEquals(x, parseExpressionOp(OpGlobalContext, LogicalContext.Compare), false))
                eatSequence("!=") -> OpNot(OpEquals(x, parseExpressionOp(OpGlobalContext, LogicalContext.Compare), true))
                !nextSequenceIs("++") && !nextSequenceIs("+=") && eat('+') -> OpAdd(x, parseTermOp(OpGlobalContext))
                !nextSequenceIs("--") && !nextSequenceIs("-=") && eat('-') -> OpSub(x, parseTermOp(OpGlobalContext))
                else -> return x
            }
        }
    }

    private fun parseTermOp(context: Expression): Expression {
        var x = parseFactorOp(context)
        while (true) {
            prepareNextChar()
            x = when {
                !nextSequenceIs("*=") && eat('*') -> OpMul(x, parseFactorOp(OpGlobalContext))
                !nextSequenceIs("/=") && eat('/') -> OpDiv(x, parseFactorOp(OpGlobalContext))
                !nextSequenceIs("%=") &&eat('%') -> OpMod(x, parseFactorOp(OpGlobalContext))
                else -> return x
            }
        }
    }

    private fun parseFactorOp(context: Expression): Expression {
        val parsedOp = when {
            context is OpGlobalContext && eatSequence("++") -> {
                val start = pos
                val variable = parseFactorOp(OpGlobalContext)
                require(variable.isAssignable()){
                    "Unexpected '++' as $start"
                }
                OpIncrement(variable)
            }

            context is OpGlobalContext && eatSequence("--") -> {
                val start = pos
                val variable = parseFactorOp(OpGlobalContext)
                require(variable.isAssignable()){
                    "Unexpected '--' as $start"
                }
                OpDecrement(variable)
            }

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

            context is OpGlobalContext && nextCharIs { it.isDigit() || it == '.' } -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    print("making const number... ")
                }
                var numberFormat = NumberFormat.Dec
                var isFloat = nextCharIs { it == '.' }
                val startPos = pos
                do {
                    nextChar()
                    when(ch.lowercaseChar()){
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
                        NumberFormat.Oct.prefix  -> {
                            check(numberFormat == NumberFormat.Dec && !isFloat) {
                                "Invalid number at pos $startPos"
                            }
                            numberFormat = NumberFormat.Oct
                        }
                        NumberFormat.Bin.prefix  -> {
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

                val num = expr.substring(startPos, pos).let {
                    if (it.endsWith('.')) {
                        prevChar()
                        isFloat = false
                    }
                    if (isFloat) {
                        it.toDouble()
                    }
                    else {
                        it.trimEnd('.')
                            .let { n -> numberFormat.prefix?.let(n::substringAfter) ?: n }
                            .toULong(numberFormat.radix)
                            .toLong()
                    }
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
                OpConstant(str)
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
                } while (
                    pos < expr.length && ch.isFun() && !(isReserved(expr.substring(startPos, pos)) && ch == ' ')
                )

                val func = expr.substring(startPos, pos).trim()

                parseFunction(context, func)
            }

            else -> error("Unsupported Lottie expression: $expr")
        }

        return parsedOp.finish()
    }

    private fun Expression.finish() : Expression {
        return when {
            // inplace function invocation
            this is ExpressionContext<*> && nextCharIs { it == '(' } -> {
                parseFunction(this, null)
            }
            // begin condition || property || index
            this is OpVar ||
                    this is OpIfCondition && this.onTrue == null
                    || eat('.')
                    || nextCharIs('['::equals) ->
                parseFactorOp(this) // continue with receiver

            else -> this
        }
    }

    private fun parseFunctionArgs(name : String?): List<Expression>? {

        if (!nextCharIs('('::equals)){
            return null
        }
        return buildList {
            when {
                eat('(') -> {
                    if (eat(')')){
                        return@buildList //empty args
                    }
                    do {
                        add(parseAssignment(OpGlobalContext))
                    } while (eat(','))

                    require(eat(')')) {
                        "Bad expression:Missing ')' after argument to $name"
                    }
                }
            }
        }
    }

    private fun parseFunction(context: Expression, func : String?) : Expression {

        return when (func) {
            "function" -> parseFunctionDefinition()
            "while" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making while loop")
                }

                OpWhileLoop(
                    condition = parseWhileCondition(),
                    body = parseBlock()
                )
            }
            "do" -> {
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making do/while loop")
                }

                val body = parseBlock()

                check(body is OpBlock){
                    "Invalid do/while syntax"
                }

                check(eatSequence("while")){
                    "Missing while condition in do/while block"
                }
                val condition = parseWhileCondition()

                OpDoWhileLoop(
                    condition = condition,
                    body = body
                )
            }

            "return" -> {
                val expr = parseExpressionOp(OpGlobalContext)
                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making return with $expr")
                }
                OpReturn(expr)
            }
            "try" -> {
                val tryBlock = parseBlock(requireBlock = true)
                val catchBlock = if (eatSequence("catch")) {

                    if (eat('(')) {
                        val start = pos
                        while (!eat(')') && pos < expr.length) {
                            //nothing
                        }
                        expr.substring(start, pos).trim() to parseBlock(
                            scoped = false,
                            requireBlock = true
                        )
                    } else {
                        null to parseBlock(requireBlock = true)
                    }
                }
                else null

                val finallyBlock = if (eatSequence("finally")){
                    parseBlock(requireBlock = true)
                } else null

                OpTryCatch(
                    tryBlock = tryBlock,
                    catchVariableName = catchBlock?.first,
                    catchBlock = catchBlock?.second,
                    finallyBlock = finallyBlock
                )
            }

            else -> {
                val args = parseFunctionArgs(func)

                if (EXPR_DEBUG_PRINT_ENABLED) {
                    println("making fun $func")
                }

                return when (context) {
                    is ExpressionContext<*> -> context.interpret(func, args)
                        ?: (if (args != null && func != null && this.context.getVariable(func) is OpFunction) {
                            if (EXPR_DEBUG_PRINT_ENABLED) {
                                println("parsed call for defined function $func")
                            }
                            OpFunctionExec(func, args)
                        } else null)
                        ?: unresolvedReference(
                            ref = func ?: "null",
                            obj = context::class.simpleName
                                ?.substringAfter("Op")
                                ?.substringBefore("Context")
                        )

                    else -> {
                        JsContext.interpret(context, func, args)
                            ?: error("Unsupported Lottie expression function: $func")
                    }
                }
            }
        }
    }

    private fun parseWhileCondition(): Expression {
        check(eat('(')){
            "Missing while loop condition"
        }

        val condition = parseExpressionOp(OpGlobalContext)

        check(eat(')')){
            "Missing closing ')' in loop condition"
        }
        return condition
    }

    private fun parseFunctionDefinition() : Expression{
        val start = pos

        while (ch != '(') {
            nextChar()
        }

        val name = expr.substring(start, pos).trim()

        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("making defined function $name")
        }

        val args = parseFunctionArgs(name).let { args ->
            args?.fastMap {
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

        checkNotNull(args){
            "Missing function args"
        }


        check(nextCharIs('{'::equals)) {
            "Missing function body at $pos"
        }


        val block = parseBlock(
            scoped = false // function scope will be used
        )

        this.context.setVariable(
            name = name,
            value = OpFunction(
                name = name,
                parameters = args,
                body = block
            ),
            type = VariableType.Const
        )
        if (EXPR_DEBUG_PRINT_ENABLED) {
            println("registered function $name")
        }
        return OpConstant(Undefined)
    }

    private fun parseBlock(scoped : Boolean = true, requireBlock : Boolean = false): Expression {
        val list =  buildList {
            if (eat('{')) {
                while (!eat('}') && pos < expr.length) {
                    add(parseAssignment(OpGlobalContext))
                    eat(';')
                }
            } else {
                if (requireBlock){
                    error("Unexpected token at $pos: block start was expected")
                }
                add(parseAssignment(OpGlobalContext))
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
