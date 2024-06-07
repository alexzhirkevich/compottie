package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Stable
fun LottieCompositionSpec.Companion.DotLottie(
    archive: suspend () -> ByteArray
) : LottieCompositionSpec = DotLottieCompositionSpec(archive)


private class DotLottieCompositionSpec(
    private val archive : suspend () -> ByteArray
) : LottieCompositionSpec {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun load(): LottieComposition {

        val fileSystem = FakeFileSystem()
        val path = "anim".toPath()

        val bytes = archive()

        fileSystem.write(path){
            write(bytes)
        }

        val entries = fileSystem.listZipEntries(path)

        val zipSystem = ZipFileSystem(bytes, fileSystem, path)

        val animations = entries[("/animations".toPath())]!!.children
        val animBytes = zipSystem.read(animations.first())
        println(animBytes.decodeToString())
        return LottieComposition.parse(animBytes.decodeToString())
    }

    override fun hashCode(): Int {
        return 31 * archive.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? DotLottieCompositionSpec)?.archive == archive
    }
}
