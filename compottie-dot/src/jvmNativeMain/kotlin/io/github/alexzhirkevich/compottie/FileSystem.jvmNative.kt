package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

@InternalCompottieApi
actual fun defaultFileSystem() : FileSystem = FileSystem.SYSTEM
//
//actual typealias FilePath = String
//
//internal actual val FilePath.key : String?
//    get() = this
//
//@OptIn(InternalCompottieApi::class)
//@Stable
//internal actual fun defaultFileReader() : FileReader = FileSystemFileReader(defaultFileSystem())
//
//@Stable
//private class FileSystemFileReader(private val fileSystem: FileSystem): FileReader {
//    override suspend fun read(path: FilePath): ByteArray {
//        return withContext(ioDispatcher()) {
//            fileSystem.read(path.toPath()) {
//                readByteArray()
//            }
//        }
//    }
//
//    override fun equals(other: Any?): Boolean {
//        return fileSystem == (other as? FileSystemFileReader)?.fileSystem
//    }
//
//    override fun hashCode(): Int {
//        return 31 * fileSystem.hashCode()
//    }
//}