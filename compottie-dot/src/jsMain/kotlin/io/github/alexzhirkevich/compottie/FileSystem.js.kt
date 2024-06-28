package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.files.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual typealias FilePath = File


internal actual val FilePath.key : String?
    get() = null

@Stable
internal actual fun defaultFileReader() : FileReader =
    FileSystemFileReader

@Stable
private object FileSystemFileReader: FileReader {

    override suspend fun read(path: FilePath): ByteArray {
        return suspendCancellableCoroutine { cont ->
            val reader = org.w3c.files.FileReader()

            reader.onload = {
                cont.resume(
                    Uint8Array(it.target.asDynamic().result.unsafeCast<ArrayBuffer>())
                        .unsafeCast<ByteArray>()
                )
            }

            reader.onerror = {
                cont.resumeWithException(
                    Exception("Failed to load file. Was it picked by user? Trace: $it")
                )
            }

            cont.invokeOnCancellation {
                reader.abort()
            }

            reader.readAsArrayBuffer(path)
        }
    }
}