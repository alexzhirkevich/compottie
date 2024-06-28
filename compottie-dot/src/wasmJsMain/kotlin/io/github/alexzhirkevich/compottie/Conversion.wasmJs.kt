package io.github.alexzhirkevich.compottie

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal fun createSourceStable(data : Int8Array) : JsAny = js("""
({
   start(c){ 
     console.log(data)
     c.enqueue(data)
     c.close()
   }
})
""")

internal fun createSourceUnstable(data : JsReference<ByteArray>) : JsAny = js("""
({
   start(c){ 
     const size = wasmExports.kotlinArraySize(data);
     const result = new Int8Array(size);
     for (let i = 0; i < size; i++) {
        result[i] = wasmExports.kotlinArrayGet(data, i);
     }
     c.enqueue(result)
     c.close()
   }
})
""")

internal fun ArrayBuffer.toByteArray(): ByteArray {
    return Int8Array(this).toByteArray()
}

@OptIn(ExperimentalCompottieApi::class)
internal fun Int8Array.toByteArray(): ByteArray {

    if (Compottie.useStableWasmMemoryManagement) {
        return ByteArray(byteLength) {
            this[it]
        }
    }
    return jsInt8ArrayToKotlinByteArray(this)
}


@OptIn(ExperimentalCompottieApi::class)
internal fun ByteArray.toInt8Array() : Int8Array {
    if (Compottie.useStableWasmMemoryManagement) {
        val result = Int8Array(size)
        for (i in indices) {
            result[i] = this[i]
        }
        return result
    }
    return byteArrayToInt8ArrayImpl(toJsReference())
}

@OptIn(ExperimentalJsExport::class)
@JsExport
private fun kotlinArrayGet(a: JsReference<ByteArray>, i: Int): Byte = a.get()[i]

@OptIn(ExperimentalJsExport::class)
@JsExport
private fun kotlinArraySize(a: JsReference<ByteArray>): Int = a.get().size


private fun byteArrayToInt8ArrayImpl(a: JsReference<ByteArray>): Int8Array = js("""{
  const size = wasmExports.kotlinArraySize(a);
  const result = new Int8Array(size);
  for (let i = 0; i < size; i++) {
     result[i] = wasmExports.kotlinArrayGet(a, i);
  }
  return result;
}""")



@JsFun(
    """ (src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }
"""
)
private external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

private fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}

