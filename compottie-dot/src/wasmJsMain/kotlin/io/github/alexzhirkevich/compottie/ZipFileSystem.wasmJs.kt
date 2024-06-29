package io.github.alexzhirkevich.compottie


import org.khronos.webgl.Int8Array

@OptIn(ExperimentalCompottieApi::class)
internal actual suspend fun decompress(array: ByteArray, decompressedSize : Int) : ByteArray {

    val ds = DecompressionStream("deflate-raw")

    val source = if (Compottie.useStableWasmMemoryManagement) {
        createSourceStable(array.toInt8Array())
    } else {
        createSourceUnstable(array.toJsReference())
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

        val chunk = Int8Array(result.value.buffer).toByteArray()

        chunk.copyInto(decompressed, ind)
        ind += array.size
    }

    return decompressed
}

