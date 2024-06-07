package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.Path

internal expect class ZipFileSystem(encoded : ByteArray, parent : FileSystem, path : Path) {
    suspend fun read(entry: ZipEntry) : ByteArray
}