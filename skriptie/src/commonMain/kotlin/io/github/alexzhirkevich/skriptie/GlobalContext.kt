package io.github.alexzhirkevich.skriptie

public interface GlobalContext<C : ScriptContext> : InterpretationContext<C> {

    public fun sum(a : Any?, b : Any?) : Any?
    public fun sub(a : Any?, b : Any?) : Any?
    public fun mul(a : Any?, b : Any?) : Any?
    public fun div(a : Any?, b : Any?) : Any?
    public fun mod(a : Any?, b : Any?) : Any?

    public fun inc(a : Any?) : Any
    public fun dec(a : Any?) : Any

    public fun neg(a : Any?) : Any?
}