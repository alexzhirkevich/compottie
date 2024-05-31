package io.github.alexzhirkevich.compottie.assets

/**
 * Used to fetch lottie assets that are not embedded to the animation JSON file
 * */
fun interface LottieAssetsFetcher {

    /**
     * Fetch asset
     *
     * @param id unique asset id that is used for referring from animation layers
     * @param path relative system path or web URL excluding file name. For example:
     *
     * - /path/to/images/
     * - https://example.com/images/
     *
     * @param name asset name and extension (for example image.png)
     * */
    suspend fun fetch(id: String, path: String, name: String): ByteArray?

    companion object {
        fun Compound(
            networkFetcher: LottieAssetsFetcher,
            localFetcher: LottieAssetsFetcher,
            cache: LottieAssetsCache
        ): LottieAssetsFetcher = CompoundLottieAssetsFetcher(networkFetcher, localFetcher, cache)
    }
}

internal val NoOpAssetsFetcher = LottieAssetsFetcher { _, _, _ -> null }


