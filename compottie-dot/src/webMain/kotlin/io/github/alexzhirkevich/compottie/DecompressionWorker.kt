package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.internal.WorkerScript
import io.github.alexzhirkevich.compottie.internal.doWork
import org.khronos.webgl.ArrayBuffer
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

private const val DeflateWebWorker = """
self.onmessage = async (e) => {
     const { id, bytes } = e.data;
     try {
        const decompressedStream = new Blob([bytes])
            .stream()
            .pipeThrough(new DecompressionStream("deflate-raw"));

        const resultBuffer = await new Response(decompressedStream)
            .arrayBuffer();
    
        self.postMessage(
            { kind: "result", id: id, buffer: resultBuffer },
            [resultBuffer]
        );
    } catch (err) {
        self.postMessage(
            { kind: "error", id: id, message: err?.message ?? String(err) }
        );
    }
}
"""

@OptIn(InternalCompottieApi::class)
private val DeflateWorker by lazy {
    WorkerScript(DeflateWebWorker)
}

@OptIn( InternalCompottieApi::class, ExperimentalWasmJsInterop::class)
internal suspend fun deflate(bytes: ByteArray): ArrayBuffer =
    DeflateWorker.doWork(bytes) { id, data ->
        DecompressionRequest(id, data)
    }

@OptIn(ExperimentalWasmJsInterop::class)
private fun DecompressionRequest(
    id: String,
    bytes: ArrayBuffer,
): JsAny = js("({ id: id, bytes: bytes })")
