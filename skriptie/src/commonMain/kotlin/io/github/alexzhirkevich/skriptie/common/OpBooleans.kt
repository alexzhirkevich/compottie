package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext

internal fun <C : ScriptContext> OpNot(
    condition : Expression<C>,
    isFalse : (Any?) -> Boolean,
) = Expression<C> {
    isFalse(condition(it))
}

internal fun <C : ScriptContext> OpBoolean(
    a : Expression<C>,
    b : Expression<C>,
    isFalse : (Any?) -> Boolean,
    op : (Boolean, Boolean) -> Boolean,
) = Expression<C> {
    op(!isFalse(a(it)), !(isFalse(b(it))))
}

