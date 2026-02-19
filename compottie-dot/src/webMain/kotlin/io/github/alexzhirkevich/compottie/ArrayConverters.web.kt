package io.github.alexzhirkevich.compottie

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

internal expect fun ByteArray.convertToInt8Array() : Int8Array
internal expect fun Int8Array.convertToByteArray() : ByteArray