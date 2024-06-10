package io.github.alexzhirkevich.compottie

import org.khronos.webgl.Uint8Array

internal actual suspend fun decompress(array: ByteArray, inflatedSize : Int) : ByteArray {

    val ds = DecompressionStream("deflate-raw")

    val source = object : UnderlyingSource {
        override fun start(controller: SourceController) {
            controller.enqueue(array);
            controller.close();
        }
    }

    val stream = ReadableStream(source)

    val reader = stream
        .pipeThrough(ds)
        .getReader()

    val inflatedResult = ArrayList<Byte>(inflatedSize)

    while (true) {
        val result = reader.read().await()
        if (result.done) {
            break
        }

        inflatedResult.addAll(
            Uint8Array(result.value.buffer).unsafeCast<Array<Byte>>()
        )
    }

    return inflatedResult.toByteArray()
}

