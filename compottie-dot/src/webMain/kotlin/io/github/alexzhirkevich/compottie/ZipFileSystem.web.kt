package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.Path

internal actual class ZipFileSystem actual constructor(
    private val encoded : ByteArray,
    private val parent : FileSystem,
    path : Path

) {

    actual suspend fun read(path: Path): ByteArray {
        TODO("DotLottie is not available for web yet")
    }
}