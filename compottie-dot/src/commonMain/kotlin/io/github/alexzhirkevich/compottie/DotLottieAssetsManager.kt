package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.assets.LottieAsset
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import okio.FileNotFoundException
import okio.IOException
import okio.Path.Companion.toPath

internal class DotLottieAssetsManager(
    private val zipFileSystem: ZipFileSystem,
) : LottieAssetsManager {

    override suspend fun fetch(asset: LottieAsset): ByteArray? {

        val trimPath = asset.path
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        val trimName = asset.name
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        load(null, trimPath, trimName)?.let {
            return it
        }

        val dir = when (asset.type) {
            LottieAsset.AssetType.Image -> "/images"
        }

        return load(dir, trimPath, trimName)
    }

    private suspend fun load(root: String?, trimPath: String?, trimName: String?): ByteArray? {
        val fullPath = listOfNotNull(root, trimPath, trimName)
            .joinToString("/")

        return try {
            zipFileSystem.read(fullPath.toPath())
        } catch (t: IOException) {
            null
        }
    }
}