
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
public fun rememberBlurHashPainter(
    hash : String,
    width : Int = 0,
    height : Int = 0,
    quality : Float = 1f,
    intensity : Float = 1f
) : BlurHashPainter {
    return remember(hash, width, height, quality, intensity) {
        BlurHashPainter(hash, width, height, quality, intensity)
    }
}

/**
 * @param hash blur hash of an image
 * @param width image width. Used for intrinsic size only
 * @param height image height. Used for intrinsic size only
 * @param quality blur quality. Must be positive
 * */
public class BlurHashPainter(
    public val hash : String,
    width : Int = 0,
    height : Int = 0,
    private val quality : Float = 1f,
    private val intensity : Float = 1f,
) : Painter() {

    override val intrinsicSize: Size = Size(
        width.takeIf { it > 0 }?.toFloat() ?: Float.NaN,
        height.takeIf { it > 0 }?.toFloat() ?: Float.NaN,
    )

    private var cachedBitmap: ImageBitmap? = null

    override fun DrawScope.onDraw() {
        val pixmapSize = (3000f * quality.coerceAtLeast(0f))
            .coerceAtLeast(100f)
        val targetScale = sqrt(pixmapSize / (size.width * size.height)).coerceAtMost(1f)

        val targetWidth = (size.width * targetScale).roundToInt()
        val targetHeight = (size.height * targetScale).roundToInt()

        val bitmap = cachedBitmap?.takeIf {
            it.width >= targetWidth && it.width.toFloat() / it.height == targetWidth.toFloat() / targetHeight
        } ?: BlurHashDecoder.decode(hash, targetWidth, targetHeight, intensity).also {
            cachedBitmap = it
        }

        drawImage(
            image = bitmap,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        )
    }
}