package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpMakeArray(
    items : List<Expression>
) = Expression { context ->
    items.fastMap { it(context) }.toMutableList()
}