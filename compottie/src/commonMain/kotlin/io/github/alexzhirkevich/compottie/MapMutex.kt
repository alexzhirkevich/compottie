package io.github.alexzhirkevich.compottie

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private class MutexCount(
    val mutex: Mutex = Mutex(),
    var waiters : Int = 0
)

@InternalCompottieApi
public class MapMutex {

    private val lock = SynchronizedObject()
    private val mutex = mutableMapOf<Any, MutexCount>()

    public suspend fun <T> withLock(key: Any, action: suspend () -> T): T {

        val keyLock = synchronized(lock) {
            mutex.getOrPut(key) { MutexCount() }.also { it.waiters++ }
        }
        Mutex().lock()

        return try {
            keyLock.mutex.withLock(key) { action() }
        } finally {
            synchronized(lock) {
                if (--keyLock.waiters <= 0) {
                    mutex.remove(key)
                }
            }
        }
    }
}