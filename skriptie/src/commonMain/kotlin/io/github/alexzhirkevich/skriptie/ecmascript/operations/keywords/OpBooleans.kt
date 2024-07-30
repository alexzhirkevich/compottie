package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext

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

