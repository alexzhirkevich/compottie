package io.github.alexzhirkevich.compottie

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

internal class LruMap<T : Any>(
    private val delegate : LinkedHashMap<Any,T> = LinkedHashMap(),
    private val limit : () -> Int,
) : SynchronizedObject(), MutableMap<Any, T> by delegate {

    @OptIn(InternalCompottieApi::class)
    private val suspendGetOrPutMutex = MultiOwnerMutex()

    override fun put(key: Any, value: T): T? = synchronized(this) {
        putRaw(key, value)
    }

    override fun clear() = synchronized(this) {
        clearRaw()
    }

    override fun putAll(from: Map<out Any, T>) = synchronized(this) {
        putAllRaw(from)
    }

    override fun remove(key: Any): T? = synchronized(this) {
        removeRaw(key)
    }

    override fun get(key: Any): T? = synchronized(this) {
        getRaw(key)
    }

    fun getOrPut(key: Any?, put: () -> T): T = synchronized(this) {
        if (key == null)
            return put()

        return getRaw(key) ?: run {
            val v = put()
            putRaw(key, v)
            v
        }
    }

    @OptIn(InternalCompottieApi::class)
    suspend fun getOrPutSuspend(key: Any, put: suspend () -> T): T {
        return suspendGetOrPutMutex.withLock(key) {
            getRaw(key) ?: put().also { putRaw(key, it) }
        }
    }

    private fun putRaw(key: Any, value: T): T? {
        val cacheLimit = limit()

        if (cacheLimit < 1) {
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