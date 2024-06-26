package io.github.alexzhirkevich.compottie

import okio.Closeable
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.random.Random
import kotlin.random.nextULong

@InternalCompottieApi
class UnsupportedFileSystemException : CompottieException("File system is not unsupported")

@InternalCompottieApi
expect fun defaultFileSystem() : FileSystem

@InternalCompottieApi
fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {}
}

@InternalCompottieApi
fun FileSystem.createFile(file: Path, mustCreate: Boolean = false) {
    if (mustCreate) {
        sink(file, mustCreate = true).closeQuietly()
    } else if (!exists(file)) {
        sink(file).closeQuietly()
    }
}

@InternalCompottieApi
fun FileSystem.createTempFile(): Path {
    var tempFile: Path
    do {
        tempFile = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tmp_${Random.nextULong()}"
    } while (exists(tempFile))
    createFile(tempFile, mustCreate = true)
    return tempFile
}

@InternalCompottieApi
fun FileSystem.deleteContents(directory: Path) {
    var exception: IOException? = null
    val files = try {
        list(directory)
    } catch (_: FileNotFoundException) {
        return
    }
    for (file in files) {
        try {
            if (metadata(file).isDirectory) {
                deleteContents(file)
            }
            delete(file)
        } catch (e: IOException) {
            if (exception == null) {
                exception = e
            }
        }
    }
    if (exception != null) {
        throw exception
    }
}