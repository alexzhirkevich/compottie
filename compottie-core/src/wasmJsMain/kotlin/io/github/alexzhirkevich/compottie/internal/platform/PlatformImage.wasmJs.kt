package io.github.alexzhirkevich.compottie.internal.platform

import kotlinx.coroutines.await
import org.jetbrains.skiko.InternalSkikoApi

@OptIn(ExperimentalWasmJsInterop::class, InternalSkikoApi::class)
internal actual suspend fun awaitSkiko(): JsAny {
    return org.jetbrains.skiko.wasm.awaitSkiko.await()
}