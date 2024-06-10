package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.Path

internal expect class ZipFileSystem(
    parent : FileSystem,
    entries: Map<Path, ZipEntry>,
    path : Path
) {
    suspend fun read(path: Path) : ByteArray
}
