package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

internal expect suspend fun decompress(array: ByteArray, decompressedSize : Int) : ByteArray

internal actual class ZipFileSystem actual constructor(
    private val parent : FileSystem,
    private val entries: Map<Path, ZipEntry>,
    private val path : Path
) {
    actual suspend fun read(path: Path) : ByteArray {
        val entry = entries[root.resolve(path, true)] ?: error("Invalid entry")

        val source = parent.openReadOnly(this.path).use { fileHandle ->
            fileHandle.source(entry.offset).buffer()
        }
        source.skipLocalHeader()

        val bytes = source.readByteArray(entry.compressedSize)

        if (entry.compressionMethod == COMPRESSION_METHOD_STORED)
            return bytes

        return decompress(bytes, entry.size.toInt())
    }

    private val root = "/".toPath()
}

