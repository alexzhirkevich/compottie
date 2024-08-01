package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.invoke

internal fun <C : ScriptRuntime>  OpTryCatch(
    tryBlock : Expression<C>,
    catchVariableName : String?,
    catchBlock : Expression<C>?,
    finallyBlock : Expression<C>?,
) = when {
    catchBlock != null && finallyBlock != null ->
        TryCatchFinally(tryBlock, catchVariableName, catchBlock, finallyBlock)

    catchBlock != null -> TryCatch(tryBlock, catchVariableName, catchBlock)
    finallyBlock != null -> TryFinally(tryBlock, finallyBlock)
    else -> error("SyntaxError: Missing catch or finally after try")
}

private fun <C : ScriptRuntime>  TryCatchFinally(
    tryBlock : Expression<C>,
    catchVariableName : String?,
    catchBlock : Expression<C>,
    finallyBlock : Expression<C>,
) = Expression<C> {
    try {
        tryBlock(it)
    } catch (t: Throwable) {
        if (catchVariableName != null) {
            it.withScope(mapOf(catchVariableName to (VariableType.Const to t))) {
                catchBlock(it as C)
            }
        } else {
            catchBlock(it)
        }
    } finally {
        finallyBlock(it)
    }
}

private fun <C : ScriptRuntime> TryCatch(
    tryBlock : Expression<C>,
    catchVariableName : String?,
    catchBlock : Expression<C>
) = Expression<C> {
    try {
        tryBlock(it)
    } catch (t: Throwable) {
        if (catchVariableName != null) {
            it.withScope(mapOf(catchVariableName to (VariableType.Const to t))) {
                catchBlock(it as C)
            }
        } else {
            catchBlock(it)
        }
    }
}


private fun <C : ScriptRuntime> TryFinally(
    tryBlock : Expression<C>,
    finallyBlock : Expression<C>,
) = Expression<C> {
    try {
        tryBlock(it)
    } finally {
        finallyBlock(it)
    }
}