package io.github.alexzhirkevich.compottie

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class LruMap<T : Any>(
    private val delegate : LinkedHashMap<Any,T> = LinkedHashMap(),
    private val limit : () -> Int,
) : MutableMap<Any, T> by delegate {

    private val suspendGetOrPutMutex = Mutex()
    private val lock = SynchronizedObject()

    override fun put(key: Any, value: T): T?  = synchronized(lock) {
        putRaw(key, value)
    }

    override fun clear() = synchronized(lock) {
        clearRaw()
    }

    override fun putAll(from: Map<out Any, T>)  = synchronized(lock) {
        putAllRaw(from)
    }

    override fun remove(key: Any): T? = synchronized(lock) {
        removeRaw(key)
    }

    override fun get(key: Any): T? = synchronized(lock) {
        getRaw(key)
    }

    fun getOrPut(key: Any?, put: () -> T): T = synchronized(lock) {
        if (key == null)
            return put()

        return getRaw(key) ?: run {
            val v = put()
            putRaw(key, v)
            v
        }
    }

    suspend fun getOrPutSuspend(key: Any?, put: suspend () -> T): T {
        return suspendGetOrPutMutex.withLock {
            if (key == null)
                return@withLock put()

            getRaw(key) ?: run {
                val v = put()
                putRaw(key, v)
                v
            }
        }
    }

    private fun putRaw(key: Any, value: T): T? {
        val cacheLimit = limit()

        if (cacheLimit < 1){
            clearRaw()
        } else {
            while (cacheLimit < size) {
                remove(keys.firstOrNull())
            }
        }

        return delegate.put(key, value)
    }

    private fun putAllRaw(from: Map<out Any, T>) {
        from.forEach {
            putRaw(it.key, it.value)
        }
    }

    private fun getRaw(key: Any): T? {
        val cached = removeRaw(key) ?: return null
        putRaw(key, cached)
        return cached
    }

    private fun removeRaw(key: Any): T? = delegate.remove(key)
    private fun clearRaw() = delegate.clear()
}