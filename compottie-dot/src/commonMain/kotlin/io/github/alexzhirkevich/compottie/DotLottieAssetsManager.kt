package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

internal class DotLottieAssetsManager(
    private val zipFileSystem: ZipFileSystem,
    private val root : Path? = null
) : LottieAssetsManager by LottieAssetsManager.Empty {

    override suspend fun image(image: LottieImage): ImageRepresentable? {

        val trimPath = image.path
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        val trimName = image.name
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        load(null, trimPath, trimName)?.let {
            return ImageRepresentable.Bytes(it)
        }

        return load("/images", trimPath, trimName)?.let {
            ImageRepresentable.Bytes(it)
        }
    }

    private suspend fun load(root: String?, trimPath: String?, trimName: String?): ByteArray? {
        val fullPath = listOfNotNull(root, trimPath, trimName)
            .joinToString("/")

        return try {
            val r = this.root ?: "/".toPath()
            zipFileSystem.read(r.resolve(fullPath.toPath(true)))
        } catch (t: IOException) {
            Compottie.logger?.error("Failed to decode dotLottie asset $trimName", t)
            return null
        }
    }
}