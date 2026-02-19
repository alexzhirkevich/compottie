package io.github.alexzhirkevich.compottie.internal

import io.github.alexzhirkevich.compottie.InternalCompottieApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toInt8Array
import org.w3c.dom.MessageEvent
import org.w3c.dom.Worker
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.js
import kotlin.js.set
import kotlin.js.unsafeCast
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@InternalCompottieApi
fun WorkerScript(code : String): Worker {
    val url = URL.createObjectURL(blob(code))
    return Worker(url).also { URL.revokeObjectURL(url) }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalWasmJsInterop::class)
@InternalCompottieApi
suspend fun Worker.doWork(
    input : ByteArray,
    message : (String, ArrayBuffer) -> JsAny
) : ArrayBuffer = suspendCancellableCoroutine { continuation ->
    val id = Uuid.random().toString()
    var responseListener: ((Event) -> Unit)? = null
    var errorListener: ((Event) -> Unit)? = null

    fun cleanup() {
        removeEventListener("message", responseListener)
        removeEventListener("error", errorListener)
    }

    responseListener = { event ->
        (event as? MessageEvent)?.data?.unsafeCast<WebWorkerResponse>()
            ?.takeIf { it.kind == "result" && it.id == id }
            ?.let {
                cleanup()
                continuation.resume(it.buffer)
            }
    }
    errorListener = { event ->
        val err = event.unsafeCast<WebWorkerError>()
        if (err.id == id) {
            cleanup()
            continuation.resumeWithException(Exception("Worker error: ${err.message}"))
        }
    }

    addEventListener("message", responseListener)
    addEventListener("error", errorListener)
    val buffer = input.toInt8Array().buffer
    val transfer = JsArray<JsAny>().apply { set(0, buffer) }
    postMessage(message(id, buffer), transfer)

    continuation.invokeOnCancellation {
        cleanup()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun blob(code: String): Blob =
    js("new Blob([code], { type: 'application/javascript' })")


@InternalCompottieApi
public external interface WebWorkerResponse : JsAny {
    val id: String
    val kind: String
    val buffer: ArrayBuffer
}

private external interface WebWorkerError : JsAny {
    val id: String
    val message: String
}