package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke

internal fun <C : ScriptRuntime> OpNot(
    condition : Expression<C>,
    isFalse : (Any?) -> Boolean,
) = Expression<C> {
    isFalse(condition(it))
}

internal fun <C : ScriptRuntime> OpBoolean(
    a : Expression<C>,
    b : Expression<C>,
    isFalse : (Any?) -> Boolean,
    op : (Boolean, Boolean) -> Boolean,
) = Expression<C> {
    op(!isFalse(a(it)), !(isFalse(b(it))))
}

