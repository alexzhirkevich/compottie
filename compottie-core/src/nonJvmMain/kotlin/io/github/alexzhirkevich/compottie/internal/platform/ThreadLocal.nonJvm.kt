package io.github.alexzhirkevich.compottie.internal.platform

internal actual class ThreadLocal<T : Any> {

    private var value : T? = null

    actual fun get(): T? = value

    actual fun set(value: T) {
        this.value = value
    }
}