package io.github.alexzhirkevich.compottie


internal class LruMap<T : Any>(
    private val delegate : LinkedHashMap<Any,T> = LinkedHashMap(),
    private val limit : Int,
) : MutableMap<Any, T> by delegate {

    @OptIn(InternalCompottieApi::class)
    private val suspendGetOrPutMutex = MultiOwnerMutex()

    override fun put(key: Any, value: T): T? = putRaw(key, value)

    override fun clear() = clearRaw()

    override fun putAll(from: Map<out Any, T>) = putAllRaw(from)


    override fun remove(key: Any): T? = removeRaw(key)


    override fun get(key: Any): T? = getRaw(key)

    fun getOrPut(key: Any?, put: () -> T): T {
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

        if (limit < 1) {
            return value
        }

        while (limit < size) {
            remove(keys.firstOrNull())
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