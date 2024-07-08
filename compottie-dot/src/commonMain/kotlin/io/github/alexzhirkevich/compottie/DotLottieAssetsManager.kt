package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImageSpec
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import okio.Path
import okio.Path.Companion.toPath

internal class DotLottieAssetsManager(
    private val zipFileSystem: ZipFileSystem,
    private val root : Path? = null
) : LottieAssetsManager {

    override suspend fun image(image: LottieImageSpec): ImageRepresentable? {

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
        } ?: run {
            Compottie.logger?.warn("Failed to decode dotLottie asset $trimName")
            null
        }
    }

    private suspend fun load(root: String?, trimPath: String?, trimName: String?): ByteArray? {
        return try {
            val fullPath = listOfNotNull(root, trimPath, trimName)
                .joinToString("/")

            val r = this.root ?: "/".toPath()
            zipFileSystem.read(r.resolve(fullPath.toPath(true)))
        } catch (t: Throwable) {
            return null
        }
    }
}