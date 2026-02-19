package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray

internal actual class ZipFileSystem actual constructor(
    private val parent : FileSystem,
    actual val entries: Map<Path, ZipEntry>,
    private val path : Path
) {

    actual suspend fun read(path: Path) : ByteArray {
        val entry = entries[root.resolve(path, true)] ?: error("Invalid entry")

        val source = parent.openReadOnly(this.path).use { fileHandle ->
            fileHandle.source(entry.offset).buffer()
        }
        source.readOrSkipLocalHeader(null)

        val bytes = source.readByteArray(entry.compressedSize)

        if (entry.compressionMethod == COMPRESSION_METHOD_STORED)
            return bytes

        return Int8Array(deflate(bytes)).toByteArray()
    }

    private val root = "/".toPath()
}

