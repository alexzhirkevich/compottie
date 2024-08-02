package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpNot(
    condition : Expression,
    isFalse : (Any?) -> Boolean,
) = Expression {
    isFalse(condition(it))
}

internal fun  OpBoolean(
    a : Expression,
    b : Expression,
    isFalse : (Any?) -> Boolean,
    op : (Boolean, Boolean) -> Boolean,
) = Expression {
    op(!isFalse(a(it)), !(isFalse(b(it))))
}

