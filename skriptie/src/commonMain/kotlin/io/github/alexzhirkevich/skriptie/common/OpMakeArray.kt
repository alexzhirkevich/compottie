package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext

internal fun <C : ScriptContext> OpMakeArray(
    items : List<Expression<C>>
) = Expression<C> { context ->
    items.fastMap { it(context) }.toMutableList()
}