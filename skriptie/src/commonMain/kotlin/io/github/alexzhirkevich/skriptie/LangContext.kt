package io.github.alexzhirkevich.skriptie

public interface LangContext {

    public fun isFalse(a : Any?) : Boolean

    public fun sum(a : Any?, b : Any?) : Any?
    public fun sub(a : Any?, b : Any?) : Any?
    public fun mul(a : Any?, b : Any?) : Any?
    public fun div(a : Any?, b : Any?) : Any?
    public fun mod(a : Any?, b : Any?) : Any?

    public fun inc(a : Any?) : Any?
    public fun dec(a : Any?) : Any?

    public fun neg(a : Any?) : Any?
    public fun pos(a : Any?) : Any?

    public fun toNumber(a: Any?, strict : Boolean = false) : Number

    public fun fromKotlin(a : Any?) : Any?
    public fun toKotlin(a : Any?) : Any?
}