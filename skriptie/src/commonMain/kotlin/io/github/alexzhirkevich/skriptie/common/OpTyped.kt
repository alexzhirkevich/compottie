package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke

internal fun OpLongInt(
    a : Expression,
    b : Expression,
    op : (Long, Int) -> Long
) = Expression {
    val an = it.toNumber(a(it)).toLong()
    val bn = it.toNumber(b(it)).toInt()

    op(an, bn)
}

internal fun OpLongLong(
    a : Expression,
    b : Expression,
    op: (Long, Long) -> Long
)  = Expression{
    val an = it.toNumber(a(it)).toLong()
    val bn = it.toNumber(b(it)).toLong()

    op(an, bn)
}