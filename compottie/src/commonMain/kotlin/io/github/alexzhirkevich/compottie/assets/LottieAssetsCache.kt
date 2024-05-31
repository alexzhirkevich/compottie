package io.github.alexzhirkevich.compottie.assets

interface LottieAssetsCache {

    suspend fun saveToCache(asset: ByteArray, id: String, path: String, name: String)

    suspend fun loadFromCache(id: String, path: String, name: String) : ByteArray?
}