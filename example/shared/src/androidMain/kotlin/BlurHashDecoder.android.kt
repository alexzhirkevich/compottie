import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun ImageBitmap.Companion.fromPixmap(
    width: Int,
    height: Int,
    colors: IntArray
) : ImageBitmap {
    return Bitmap
        .createBitmap(colors, width, height, Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}