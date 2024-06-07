//package io.github.alexzhirkevich.compottie
//
//import okio.FileSystem
//import okio.Path
//import org.khronos.webgl.ArrayBufferView
//import org.khronos.webgl.Uint8Array
//import org.w3c.files.Blob
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//import kotlin.coroutines.suspendCoroutine
//import kotlin.js.Promise
//
//external fun require(lib : String) : dynamic
//
//internal suspend fun decompress(array: ByteArray) : ByteArray {
//
//    val ds = DecompressionStream("deflate-raw")
//
//    val reader = Blob(array.toTypedArray())
//        .asDynamic()
//        .stream()
//        .pipeThrough(ds)
//        .getReader()
//        .read() as Promise<*>
//
//    return suspendCoroutine { cont ->
//        reader.then {
//            val value = it.asDynamic().value as ArrayBufferView
//            cont.resume(Uint8Array(value.buffer).unsafeCast<ByteArray>())
//        }.catch {
//            cont.resumeWithException(it)
//        }
//    }
//}
//
//external class DecompressionStream(alg : String)
//
//internal actual class ZipFileSystem actual constructor(
//    private val encoded : ByteArray,
//    private val parent : FileSystem,
//    path : Path
//
//) {
//    actual suspend fun read(path: Path): ByteArray {
//        TODO("DotLottie is not available for JS yet")
//    }
//}