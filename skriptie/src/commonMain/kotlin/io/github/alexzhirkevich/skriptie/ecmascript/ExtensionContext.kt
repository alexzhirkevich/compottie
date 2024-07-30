package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext

public interface ExtensionContext<C : ScriptContext> {

    public fun interpret(parent: Expression<C>, op: String?, args: List<Expression<C>>?): Expression<C>?
}
