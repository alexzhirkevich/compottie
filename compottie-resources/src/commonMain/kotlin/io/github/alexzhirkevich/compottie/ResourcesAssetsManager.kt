package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie_resources.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.MissingResourceException

/**
 * Compose resources Lottie asset manager.
 *
 * Assess are stored in the _**composeResources/[directory]**_ directory.
 *
 * Handles the following possible cases:
 * - path="/images/", name="image.png"
 * - path="images/", name="image.png"
 * - path="", name="/images/image.png"
 * - path="", name="images/image.png"
 * */
@Composable
@ExperimentalCompottieApi
fun rememberResourcesAssetsManager(
    directory : String = "files",
    readBytes : suspend (path : String) -> ByteArray,
) : LottieAssetsManager {

    val updatedReadBytes by rememberUpdatedState(readBytes)

    return remember(directory) {
        ResourcesAssetsManager(directory) {
            updatedReadBytes(it)
        }
    }
}

/**
 * Compose resources asset manager.
 *
 * Assess are stored in the _**composeResources/[relativeTo]**_ directory.
 *
 * Handles the following possible cases:
 * - path="/images/", name="image.png"
 * - path="images/", name="image.png"
 * - path="", name="/images/image.png"
 * - path="", name="images/image.png"
 * */
@OptIn(ExperimentalResourceApi::class)
private class ResourcesAssetsManager(
    private val relativeTo : String = "files",
    private val readBytes : suspend (path : String) -> ByteArray = Res::readBytes,
) : LottieAssetsManager by LottieAssetsManager.Empty {
    override suspend fun image(image: LottieImage): ImageRepresentable? {
        return try {
            val trimPath = image.path
                .removePrefix("/")
                .removeSuffix("/")
                .takeIf(String::isNotEmpty)

            val trimName = image.name
                .removePrefix("/")
                .removeSuffix("/")
                .takeIf(String::isNotEmpty)

            val fullPath = listOfNotNull(
                relativeTo.takeIf(String::isNotEmpty),
                trimPath,
                trimName
            ).joinToString("/")

            ImageRepresentable.Bytes(readBytes(fullPath))
        } catch (x: MissingResourceException) {
            null
        }
    }


}