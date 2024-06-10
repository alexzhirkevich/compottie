package io.github.alexzhirkevich.compottie


import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.time.measureTime

internal actual suspend fun decompress(array: ByteArray, inflatedSize : Int) : ByteArray {

    val ds = DecompressionStream("deflate-raw")

    val stream = ReadableStream(createSource(copyToInt8Array(array)))

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
            copyToByteArray(Int8Array(result.value.buffer))
        )
    }

    return inflatedResult.toByteArray()
}

internal fun copyToByteArray(array: Int8Array): List<Byte> {
    var res : List<Byte>
    measureTime {
        res =  List(array.byteLength) {
            array[it]
        }
    }.also {
        println("Copy to js array. Size: ${array.byteLength}. Time: $it")
    }

    return res
}

private fun copyToInt8Array(array: ByteArray): Int8Array {
    val result = Int8Array(array.size)
    measureTime {
        for (i in array.indices) {
            result[i] = array[i]
        }
    }.also {
        println("Copy from js array. Size: ${array.size}. Time:$it")
    }
    return result
}