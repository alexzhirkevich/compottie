package io.github.alexzhirkevich.compottie.internal.platform


internal expect class ThreadLocal<T : Any>(){
    fun get() : T?

    fun set(value : T)
}

internal inline fun <T : Any> ThreadLocal<T>.getOrSet(default: () -> T): T {
    return get() ?: default().also(this::set)
}
