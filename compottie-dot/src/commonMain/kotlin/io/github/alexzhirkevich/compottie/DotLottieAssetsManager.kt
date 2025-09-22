package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieImageSpec
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path
import okio.Path.Companion.toPath

internal class DotLottieAssetsManager(
    private val zipFileSystem: ZipFileSystem,
    private val root : Path? = null
) : LottieAssetsManager {

    private val mutex = Mutex()

    override suspend fun image(image: LottieImageSpec): ImageRepresentable? {
        return mutex.withLock {
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

            val imagesDir = when  {
                zipFileSystem.entries.any { it.key.name == "images" && it.value.isDirectory } -> "images"
                zipFileSystem.entries.any { it.key.name == "i" && it.value.isDirectory } -> "i"
                else -> error("Failed to decode .lottie file: unrecognized format")
            }

            load("/$imagesDir", trimPath, trimName)?.let {
                ImageRepresentable.Bytes(it)
            } ?: run {
                Compottie.logger?.warn("Failed to decode dotLottie asset $trimName")
                null
            }
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