package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieImageSpec
import org.jetbrains.compose.resources.MissingResourceException

/**
 * Compose resources Lottie asset manager.
 *
 * Assess must be stored in the _**composeResources/[directory]**_.
 *
 * [`Res.readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as a [readBytes] source
 *
 * Handles the following possible cases:
 * - path="/images/", name="image.png"
 * - path="images/", name="image.png"
 * - path="", name="/images/image.png"
 * - path="", name="images/image.png"
 *
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
 * Factory method to create Compose resources [LottieAssetsManager] from non-composable
 * context.
 *
 * [`Res::readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as a [readBytes] source
 *
 * Use [rememberResourcesAssetsManager] to create it from the composition
 *
 * */
@ExperimentalCompottieApi
@Stable
fun ResourcesAssetsManager(
    directory : String = "files",
    readBytes : suspend (path : String) -> ByteArray,
) : LottieAssetsManager = ResourcesAssetsManagerImpl(directory, readBytes)


@Stable
private class ResourcesAssetsManagerImpl(
    private val directory : String = "files",
    private val readBytes : suspend (path : String) -> ByteArray,
) : LottieAssetsManager {

    override suspend fun image(image: LottieImageSpec): ImageRepresentable? {
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
                directory.takeIf(String::isNotEmpty),
                trimPath,
                trimName
            ).joinToString("/")

            ImageRepresentable.Bytes(readBytes(fullPath))
        } catch (x: MissingResourceException) {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ResourcesAssetsManagerImpl

        if (directory != other.directory) return false
        if (readBytes != other.readBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = directory.hashCode()
        result = 31 * result + readBytes.hashCode()
        return result
    }
}