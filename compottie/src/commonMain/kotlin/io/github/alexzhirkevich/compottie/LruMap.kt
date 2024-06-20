package io.github.alexzhirkevich.compottie

internal class LruMap<T : Any>(
    private val delegate : LinkedHashMap<Any,T> = LinkedHashMap(),
    private val limit : () -> Int,
) : MutableMap<Any, T> by delegate {

    override fun put(key: Any, value: T): T? {
        val cacheLimit = limit()

        while (cacheLimit in 1..size) {
            remove(keys.firstOrNull())
        }

        return delegate.put(key, value)
    }

    override fun get(key: Any): T? {
        val cached = remove(key) ?: return null

        put(key, cached)
        return cached
    }

    inline fun getOrPut(key: Any?, put: () -> T): T {
        if (key == null)
            return put()

        return get(key) ?: run {
            val v = put()
            put(key, v)
            v
        }
    }
}