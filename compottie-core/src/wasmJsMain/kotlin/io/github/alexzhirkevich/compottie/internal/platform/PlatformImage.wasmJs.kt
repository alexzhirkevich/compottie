package io.github.alexzhirkevich.compottie.internal.platform

import kotlinx.coroutines.await

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("INVISIBLE_REFERENCE")
internal actual suspend fun awaitSkiko(): JsAny {
    return org.jetbrains.skiko.wasm.awaitSkiko.await()
}