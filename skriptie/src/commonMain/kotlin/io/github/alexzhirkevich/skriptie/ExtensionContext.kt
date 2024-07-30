package io.github.alexzhirkevich.skriptie

public interface ExtensionContext<C : ScriptContext> {

    public fun interpret(parent: Expression<C>, op: String?, args: List<Expression<C>>?): Expression<C>?
}
