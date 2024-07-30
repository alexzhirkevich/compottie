package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext

internal fun <C : ScriptContext> OpNot(
    condition : Expression<C>
) = Expression<C>{
    !(condition(it) as Boolean)
}

internal fun <C : ScriptContext> OpBoolean(
    a : Expression<C>,
    b : Expression<C>,
    op : (Boolean, Boolean) -> Boolean,
) = Expression<C> {
    op(!a(it).isFalse(), !b(it).isFalse())
}

