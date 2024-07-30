package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@InternalCompottieApi
public class MapMutex {
    private val lock = Mutex()
    private val mapLock = mutableMapOf<Any, Mutex>()

    public suspend fun <T> withLock(key: Any, action: suspend () -> T): T {
        return try {
            lock.withLock { mapLock.getOrPut(key, ::Mutex) }.withLock { action() }
        } finally {
            lock.withLock { mapLock.remove(key) }
        }
    }
}