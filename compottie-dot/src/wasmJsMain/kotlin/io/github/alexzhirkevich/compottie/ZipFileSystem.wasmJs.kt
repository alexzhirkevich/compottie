package io.github.alexzhirkevich.compottie


import org.khronos.webgl.Int8Array

@OptIn(ExperimentalCompottieApi::class)
internal actual suspend fun decompress(array: ByteArray, inflatedSize : Int) : ByteArray {

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

    val inflatedResult = ArrayList<Byte>(inflatedSize)

    while (true) {
        val result = reader.read().await()
        if (result.done) {
            break
        }

        inflatedResult += Int8Array(result.value.buffer).toByteArray()
    }

    return inflatedResult.toByteArray()
}

