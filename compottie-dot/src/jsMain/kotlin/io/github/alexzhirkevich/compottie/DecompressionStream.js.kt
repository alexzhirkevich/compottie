package io.github.alexzhirkevich.compottie

import org.khronos.webgl.ArrayBufferView
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

internal external class DecompressionStream(alg : String)

internal external interface UnderlyingSource {
    fun start(controller: SourceController)
}

internal external interface SourceController {
    fun enqueue(data : ByteArray)

    fun close()
}

internal external class ReadableStream(
    source: UnderlyingSource
) {
    fun pipeThrough(decompressionStream: DecompressionStream) : ReadableStream

    fun getReader() : StreamReader
}

internal external interface StreamReader {
    fun read() : Promise<StreamReadResult>
}

internal external interface StreamReadResult {
    val done : Boolean

    val value : ArrayBufferView
}

internal suspend fun <T> Promise<T>.await() = suspendCoroutine { cont ->
    then { cont.resume(it) }
        .catch { cont.resumeWithException(Exception(it.toString())) }
}