package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private class RefCountMutex(
    val mutex: Mutex = Mutex(),
    var waiters : Int = 0
)

@InternalCompottieApi
public class MultiOwnerMutex {

    private val lock = Mutex()
    private val mutex = mutableMapOf<Any, RefCountMutex>()

    public suspend fun <T> withLock(key: Any, action: suspend () -> T): T {

        val keyLock = lock.withLock {
            mutex.getOrPut(key) { RefCountMutex() }.also { it.waiters++ }
        }

        return try {
            keyLock.mutex.withLock(key) { action() }
        } finally {
            lock.withLock {
                if (--keyLock.waiters <= 0) {
                    mutex.remove(key)
                }
            }
        }
    }
}