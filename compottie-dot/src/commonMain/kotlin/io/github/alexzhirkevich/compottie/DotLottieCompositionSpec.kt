package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

/**
 * [LottieComposition] from a dotLottie zip archive.
 *
 * @param archive archive bytes supplier
 * @param assetsManager required only if animation contains assets that are not included to the archive
 * */
@Stable
fun LottieCompositionSpec.Companion.DotLottie(
    assetsManager: LottieAssetsManager = LottieAssetsManager,
    archive: suspend () -> ByteArray,
) : LottieCompositionSpec = DotLottieCompositionSpec(archive, assetsManager)


@OptIn(ExperimentalSerializationApi::class)
private val DotLottieJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowTrailingComma = true
}

private class DotLottieCompositionSpec(
    private val archive : suspend () -> ByteArray,
    private val assetsManager: LottieAssetsManager
) : LottieCompositionSpec {

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(): LottieComposition {

        val fileSystem = FakeFileSystem()
        val path = "anim".toPath()

        val bytes = archive()

        fileSystem.write(path) {
            write(bytes)
        }

        val zipSystem = ZipFileSystem(bytes, fileSystem, path)

        val manifest = DotLottieJson.decodeFromString<DotLottieManifest>(
            zipSystem.read("manifest.json".toPath()).decodeToString()
        )

        val animation = manifest.animations.first()

        val anim = zipSystem.read("animations/${animation.id}.json".toPath())

        return LottieComposition.parse(anim.decodeToString()).apply {

            speed = animation.speed
            if (animation.loop) {
                iterations = LottieConstants.IterateForever
            }
            prepare(DotLottieAssetsManager(zipSystem))
            prepare(assetsManager)
        }
    }

    override fun hashCode(): Int {
        return 31 * archive.hashCode() + assetsManager.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? DotLottieCompositionSpec)?.let {
            it.archive == archive &&
                    it.assetsManager == assetsManager
        } == true
    }
}
