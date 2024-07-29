package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.VariableType

internal fun OpTryCatch(
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

private fun TryCatchFinally(
    tryBlock : Expression,
    catchVariableName : String?,
    catchBlock : Expression,
    finallyBlock : Expression,
) = Expression { property, context, state ->
    try {
        tryBlock(property, context, state)
    } catch (t: Throwable) {
        if (catchVariableName != null) {
            context.withScope(mapOf(catchVariableName to (VariableType.Const to t))) {
                catchBlock(property, it, state)
            }
        } else {
            catchBlock(property, context, state)
        }
    } finally {
        finallyBlock(property, context, state)
    }
}

private fun TryCatch(
    tryBlock : Expression,
    catchVariableName : String?,
    catchBlock : Expression
) = Expression { property, context, state ->
    try {
        tryBlock(property, context, state)
    } catch (t: Throwable) {
        if (catchVariableName != null) {
            context.withScope(mapOf(catchVariableName to (VariableType.Const to t))) {
                catchBlock(property, it, state)
            }
        } else {
            catchBlock(property, context, state)
        }
    }
}


private fun TryFinally(
    tryBlock : Expression,
    finallyBlock : Expression,
) = Expression { property, context, state ->
    try {
        tryBlock(property, context, state)
    } finally {
        finallyBlock(property, context, state)
    }
}