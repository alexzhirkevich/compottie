package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.Path
import okio.openZip


internal actual class ZipFileSystem actual constructor(
    encoded : ByteArray,
    parent : FileSystem,
    path : Path
) {

    private val zipFileSystem = parent.openZip(path)

    actual suspend fun read(entry: ZipEntry) : ByteArray {
        return zipFileSystem.read(entry.canonicalPath) {
            readByteArray()
        }
    }
}
