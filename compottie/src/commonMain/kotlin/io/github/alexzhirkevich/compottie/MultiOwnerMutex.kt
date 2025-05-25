package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@InternalCompottieApi
public class MultiOwnerMutex {

    private val lock = Mutex()
    private val mutex = mutableMapOf<Any, Mutex>()

    public suspend fun <T> withLock(key: Any, action: suspend () -> T): T {
        return lock
            .withLock {
                mutex.getOrPut(key, ::Mutex)
            }
            .withLock {
                action().also {
                    lock.withLock {
                        mutex.remove(key)
                    }
                }
            }
    }
}