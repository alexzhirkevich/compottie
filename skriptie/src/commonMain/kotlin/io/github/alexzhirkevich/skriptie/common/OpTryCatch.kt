package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpTryCatch(
    tryBlock : Expression,
    catchVariableName : String?,
    catchBlock : Expression?,
    finallyBlock : Expression?,
) = when {
    catchBlock != null && finallyBlock != null ->
        TryCatchFinally(tryBlock, catchVariableName, catchBlock, finallyBlock)

    catchBlock != null -> TryCatch(tryBlock, catchVariableName, catchBlock)
    finallyBlock != null -> TryFinally(tryBlock, finallyBlock)
    else -> error("SyntaxError: Missing catch or finally after try")
}

private fun  TryCatchFinally(
    tryBlock : Expression,
    catchVariableName : String?,
    catchBlock : Expression,
    finallyBlock : Expression,
) = Expression {
    try {
        tryBlock(it)
    } catch (t: Throwable) {
        if (catchVariableName != null) {
            it.withScope(
                extraVariables = mapOf(catchVariableName to (VariableType.Const to t)),
                block = catchBlock::invoke
            )
        } else {
            catchBlock(it)
        }
    } finally {
        finallyBlock(it)
    }
}

private fun TryCatch(
    tryBlock : Expression,
    catchVariableName : String?,
    catchBlock : Expression
) = Expression {
    try {
        tryBlock(it)
    } catch (t: Throwable) {
        if (catchVariableName != null) {
            it.withScope(
                extraVariables = mapOf(catchVariableName to (VariableType.Const to t)),
                block = catchBlock::invoke
            )
        } else {
            catchBlock(it)
        }
    }
}


private fun TryFinally(
    tryBlock : Expression,
    finallyBlock : Expression,
) = Expression {
    try {
        tryBlock(it)
    } finally {
        finallyBlock(it)
    }
}