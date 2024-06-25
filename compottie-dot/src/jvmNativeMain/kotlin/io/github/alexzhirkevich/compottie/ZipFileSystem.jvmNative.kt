package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.openZip

internal actual class ZipFileSystem actual constructor(
    parent : FileSystem,
    entries: Map<Path, ZipEntry>,
    path : Path
) {

    private val zipFileSystem = parent.openZip(path)

    actual suspend fun read(path: Path): ByteArray {
        return withContext(Dispatchers.IO) {
            zipFileSystem.read(path) {
                readByteArray()
            }
        }
    }
}
