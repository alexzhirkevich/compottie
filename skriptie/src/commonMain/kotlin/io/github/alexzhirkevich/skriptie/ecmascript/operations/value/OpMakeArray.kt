package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.fastMap

internal fun <C : ScriptContext> OpMakeArray(
    items : List<Expression<C>>
) = Expression<C> { context ->
    items.fastMap { it(context) }.toMutableList()
}