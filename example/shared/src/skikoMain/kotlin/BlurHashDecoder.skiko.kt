
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap

internal actual fun ImageBitmap.Companion.fromPixmap(
    width: Int,
    height: Int,
    colors: IntArray
)  : ImageBitmap {

    val bgra = ByteArray(colors.size * 4) {
        colors[it / 4].ushr((it % 4) * 8).toByte()
    }

    return ImageBitmap(width, height, ImageBitmapConfig.Argb8888)
        .asSkiaBitmap().apply { installPixels(bgra) }
        .asComposeImageBitmap()
}
