package io.github.alexzhirkevich.compottie

import org.khronos.webgl.Int8Array
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator


internal actual fun Int8Array.convertToByteArray(): ByteArray {
    return jsInt8ArrayToKotlinByteArray(this)
}

@OptIn(ExperimentalCompottieApi::class, ExperimentalWasmJsInterop::class)
internal actual fun ByteArray.convertToInt8Array() : Int8Array {
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
        new Int8Array(wasmExports.memory.buffer, dstAddr, size).set(src);
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
