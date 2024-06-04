package io.github.alexzhirkevich.compottie.assets

internal class CompoundLottieAssetsFetcher(
    private val localFetcher: LottieAssetsManager,
    private val remoteFetcher : LottieAssetsManager,
    private val cache : LottieAssetsCache
) : LottieAssetsManager {

    override suspend fun fetch(id: String, path: String, name: String): ByteArray? {
        return if (path.startsWith("https://", true) ||
            path.startsWith("http://", true)
        ) {
            cache.loadFromCache(id, path, name)?.let {
                return it
            }

            remoteFetcher.fetch(id, path, name)?.also {
                cache.saveToCache(it, id, path, name)
            }

        } else {
            localFetcher.fetch(id, path, name)
        }
    }
}