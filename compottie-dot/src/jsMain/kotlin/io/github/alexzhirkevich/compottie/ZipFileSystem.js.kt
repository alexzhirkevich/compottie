package io.github.alexzhirkevich.compottie

import org.khronos.webgl.Uint8Array

internal actual suspend fun decompress(array: ByteArray, decompressedSize : Int) : ByteArray {

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

    val decompressed = ByteArray(decompressedSize)
    var ind = 0

    while (true) {
        val result = reader.read().await()
        if (result.done) {
            break
        }

        val chunk = Uint8Array(result.value.buffer).unsafeCast<ByteArray>()

        chunk.copyInto(decompressed, ind)
        ind += chunk.size
    }

    return decompressed
}

