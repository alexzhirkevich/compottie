package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke

internal fun <C : ScriptRuntime> OpMakeArray(
    items : List<Expression<C>>
) = Expression<C> { context ->
    items.fastMap { it(context) }.toMutableList()
}