package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.LocalResourceReader
import org.jetbrains.compose.resources.ResourceReader

/**
 * Composition spec from a local resource stored by [path].
 * The format can be both JSON and dotLottie.
 * The [reader] should be a stable remembered lambda or function reference
 *
 * Example:
 *
 * ```kotlin
 * val composition by rememberLottieComposition(
 *      LottieCompositionSpec.Resource(
 *          path = "files/anim.lottie",
 *          reader = Res::readBytes
 *      )
 * )
 * ```
 * */
@ExperimentalCompottieApi
public fun LottieCompositionSpec.Companion.Resource(
    path : String,
    reader : suspend (path : String) -> ByteArray
) : LottieCompositionSpec = ResourcesCompositionSpec(path, reader)

/**
 * Composition spec from a Compose Multiplatform resource stored by [uri].
 * The format can be both JSON and dotLottie.
 *
 * Example:
 *
 * ```kotlin
 * val composition by rememberLottieComposition(
 *      LottieCompositionSpec.Resource(
 *          Res.getUri("files/anim.lottie")
 *      )
 * )
 * ```
 * */
@Suppress("ComposableNaming")
@OptIn(ExperimentalResourceApi::class)
@ExperimentalCompottieApi
@Composable
public fun LottieCompositionSpec.Companion.Resource(
    uri : String,
    directory : String = "composeResources",
    reader: ResourceReader = LocalResourceReader.current,
) : LottieCompositionSpec = remember(uri, reader, directory) {
    Resource(
        path = uri.substring(uri.indexOf(directory).takeIf { it >= 0 } ?: 0),
        reader = reader::read
    )
}

private class ResourcesCompositionSpec(
    val path : String,
    val reader : suspend (path : String) -> ByteArray
) : LottieCompositionSpec {

    override val key: String? = "res_$path"

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(): LottieComposition =
        reader(path).decodeToLottieComposition()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ResourcesCompositionSpec

        if (path != other.path) return false
        if (reader != other.reader) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + reader.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResourcesCompositionSpec(path='$path')"
    }

}