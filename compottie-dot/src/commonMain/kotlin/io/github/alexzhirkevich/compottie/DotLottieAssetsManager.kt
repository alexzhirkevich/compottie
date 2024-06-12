package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFont
import okio.IOException
import okio.Path.Companion.toPath

internal class DotLottieAssetsManager(
    private val zipFileSystem: ZipFileSystem,
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
            zipFileSystem.read(fullPath.toPath())
        } catch (t: IOException) {
            null
        }
    }
}