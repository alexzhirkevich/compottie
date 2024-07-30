package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType

internal fun <C : ScriptContext>  OpTryCatch(
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

private fun <C : ScriptContext>  TryCatchFinally(
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

private fun <C : ScriptContext> TryCatch(
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


private fun <C : ScriptContext> TryFinally(
    tryBlock : Expression<C>,
    finallyBlock : Expression<C>,
) = Expression<C> {
    try {
        tryBlock(it)
    } finally {
        finallyBlock(it)
    }
}