package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.unit.IntSize
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
    bytes: ByteArray
) : ImageBitmap {


    val size = getOriginalSize(bytes)
    val webBitmap = decodeImage(bytes, size.width, size.height)

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
    val imageInfo = ImageInfo(colorInfo, size.width, size.height)
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

internal fun getOriginalSize(bytes: ByteArray): IntSize {
    val pngSize = getPngSizeOrNull(bytes)
    if (pngSize != null) return pngSize

    val jpegSize = getJpegSizeOrNull(bytes)
    if (jpegSize != null) return jpegSize

    val webpSize = getWebpSizeOrNull(bytes)
    if (webpSize != null) return webpSize

    val image = Image.makeFromEncoded(bytes)
    return IntSize(image.width,image.height)
}

internal fun getPngSizeOrNull(bytes: ByteArray): IntSize? {
    if (bytes.size < 24) return null
    if (
        int32(bytes[0], bytes[1], bytes[2], bytes[3]) == 0x8950_4E47u &&
        int32(bytes[4], bytes[5], bytes[6], bytes[7]) == 0x0D0A_1A0Au
    ) {
        val width = int32(bytes[16], bytes[17], bytes[18], bytes[19])
        val height = int32(bytes[20], bytes[21], bytes[22], bytes[23])
        return IntSize(width.toInt(), height.toInt())
    }
    return null
}

internal fun getJpegSizeOrNull(bytes: ByteArray): IntSize? {
    if (bytes.size < 10) return null
    if (int16(bytes[0], bytes[1]) == 0xFFD8u) {
        var offset = 2
        while (offset < bytes.size - 6) {
            val marker = int16(bytes[offset], bytes[offset + 1])
            offset += 2

            if (marker in 0xFFC0u..0xFFCFu && marker != 0xFFC4u && marker != 0xFFC8u && marker != 0xFFCCu) {
                val height = int16(bytes[offset + 3], bytes[offset + 4])
                val width = int16(bytes[offset + 5], bytes[offset + 6])
                return IntSize(width.toInt(), height.toInt())
            }

            val segmentLength = int16(bytes[offset], bytes[offset + 1])
            offset += segmentLength.toInt()
        }
    }
    return null
}

internal fun getWebpSizeOrNull(bytes: ByteArray): IntSize? {
    if (bytes.size < 30) return null

    // check "RIFF" and "WEBP" signatures
    if (
        int32(bytes[0], bytes[1], bytes[2], bytes[3]) != 0x5249_4646u ||
        int32(bytes[8], bytes[9], bytes[10], bytes[11]) != 0x5745_4250u
    ) {
        return null
    }

    val chunkType = int32(bytes[12], bytes[13], bytes[14], bytes[15])

    return when (chunkType) {
        0x5650_3858u -> { // "VP8X" (Extended WebP)
            val w = int24LE(bytes[24], bytes[25], bytes[26]).toInt() + 1
            val h = int24LE(bytes[27], bytes[28], bytes[29]).toInt() + 1
            IntSize(w,h)
        }
        0x5650_3820u -> { // "VP8" (Lossy WebP)
            // Check Sync Code "0x9D 0x01 0x2A"
            if (bytes[23].asInt() != 0x9Du || bytes[24].asInt() != 0x01u || bytes[25].asInt() != 0x2Au) return null
            val w = (int16LE(bytes[26], bytes[27]) and 0x3FFFu).toInt()
            val h = (int16LE(bytes[28], bytes[29]) and 0x3FFFu).toInt()
            IntSize(w,h)
        }
        0x5650_384Cu -> { // "VP8L" (Lossless WebP)
            // Check Lossless
            if (bytes[20].asInt() != 0x2Fu) return null
            val bits = int32LE(bytes[21], bytes[22], bytes[23], bytes[24])
            val w = (bits and 0x3FFFu).toInt() + 1
            val h = ((bits shr 14) and 0x3FFFu).toInt() + 1
            IntSize(w,h)
        }
        else -> null
    }
}

private fun int16(b1: Byte, b2: Byte): UInt =
    (b1.asInt() shl 8) or b2.asInt()

private fun int24(b1: Byte, b2: Byte, b3: Byte): UInt =
    (b1.asInt() shl 16) or (b2.asInt() shl 8) or b3.asInt()

private fun int32(b1: Byte, b2: Byte, b3: Byte, b4: Byte): UInt =
    (int16(b1, b2) shl 16) or int16(b3, b4)

private fun int16LE(b1: Byte, b2: Byte): UInt =
    int16(b2, b1)

private fun int24LE(b1: Byte, b2: Byte, b3: Byte): UInt =
    int24(b3, b2, b1)

private fun int32LE(b1: Byte, b2: Byte, b3: Byte, b4: Byte): UInt =
    int32(b4, b3, b2, b1)

private fun Byte.asInt() = this.toUInt() and 0xFFu


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
