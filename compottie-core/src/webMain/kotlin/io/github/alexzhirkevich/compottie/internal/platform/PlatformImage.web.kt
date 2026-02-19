package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.internal.WorkerScript
import io.github.alexzhirkevich.compottie.internal.doWork
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.NativePointer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js

internal actual suspend fun ImageBitmap.Companion.fromBytes(
    bytes: ByteArray,
    width : Int,
    height : Int
) : ImageBitmap {

    val webBitmap = decodeImage(bytes, width, height)

    val skikoData = runCatching {
        webBitmap.passToSkiko()
    }.getOrElse {
        Data.makeFromBytes(Int8Array(webBitmap).toByteArray())
    }

    val colorInfo = ColorInfo(
        ColorType.RGBA_8888,
        ColorAlphaType.UNPREMUL,
        ColorSpace.sRGB,
    )
    val imageInfo = ImageInfo(colorInfo, width, height)
    val image = Image.makeRaster(imageInfo, skikoData, imageInfo.minRowBytes)
    return Bitmap.makeFromImage(image).asComposeImageBitmap()
}


private const val DecodeImageWorkerCode = """
let canvas = null;
let context = null;
let cw = 0, ch = 0;

function ensureCanvas(w, h) {
  if (!canvas || w > cw || h > ch) {
    cw = w; ch = h;
    canvas = new OffscreenCanvas(w, h);
    context = canvas.getContext("2d", { willReadFrequently: true });
    context.setTransform(1, 0, 0, 1, 0, 0);
  }
  return context;
}

self.onmessage = async (e) => {
    const { id, data, w, h } = e.data;
    try {
        var blob = new Blob([data]);
        const bmp = await createImageBitmap(blob, {
            resizeWidth: w,
            resizeHeight: h,
            resizeQuality: 'high'
        });
        const ctx = ensureCanvas(w, h);
        ctx.clearRect(0, 0, w, h);
        ctx.drawImage(bmp, 0, 0);
        bmp.close();

        const imgData = ctx.getImageData(0, 0, w, h);
        const rawBuffer = imgData.data.buffer;
        self.postMessage(
            { kind: "result", id: id, buffer: rawBuffer },
            [rawBuffer]
        );
    } catch (err) {
        self.postMessage(
            { kind: "error", id: id, message: err?.message ?? String(err), }
        );
    }
};
"""

@OptIn(InternalCompottieApi::class)
private val DecodeImageWorker by lazy { WorkerScript(DecodeImageWorkerCode) }

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun ArrayBuffer.passToSkiko(): Data {
    val data = Data.makeUninitialized(byteLength)
    getSkikoMemory(awaitSkiko()).set(this, data.writableData())
    return data
}

@OptIn(ExperimentalWasmJsInterop::class)
internal expect suspend fun awaitSkiko(): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
private fun getSkikoMemory(skikoWasm: JsAny): ArrayBuffer =
    js("skikoWasm.wasmExports.memory.buffer")

private fun ArrayBuffer.set(data: ArrayBuffer, offset: NativePointer) {
    Int8Array(this).set(Int8Array(data), offset)
}

@OptIn(ExperimentalWasmJsInterop::class, InternalCompottieApi::class)
private suspend fun decodeImage(
    bytes: ByteArray,
    width: Int,
    height: Int,
): ArrayBuffer = DecodeImageWorker.doWork(bytes) { id, data ->
    DecodeImageRequest(id, data, width, height)
}

private fun DecodeImageRequest(
    id: String,
    buffer: ArrayBuffer,
    width: Int,
    height: Int,
): JsAny = js("({ id: id, data: buffer, w: width, h: height })")
