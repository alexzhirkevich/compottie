package io.github.alexzhirkevich.compottie.assets

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.alexzhirkevich.compottie.internal.assets.resize
import io.github.alexzhirkevich.compottie.internal.platform.fromBytes
import androidx.compose.ui.graphics.painter.Painter as ComposePainter

public interface ImageRepresentable {

    public fun toBitmap(width: Int, height: Int): ImageBitmap

    public class Bytes(private val bytes: ByteArray) : ImageRepresentable {
        override fun toBitmap(width: Int, height: Int): ImageBitmap {
            return ImageBitmap.fromBytes(bytes).resize(width, height)
        }
    }

    public class Painter(private val painter: ComposePainter) : ImageRepresentable {
        override fun toBitmap(width: Int, height: Int): ImageBitmap {
            return painter.toBitmap(width, height)
        }
    }

    public class Bitmap(private val bitmap: ImageBitmap) : ImageRepresentable {
        override fun toBitmap(width: Int, height: Int): ImageBitmap {
            return bitmap.resize(width, height)
        }
    }
}

private fun ComposePainter.toBitmap(w : Int, h : Int) : ImageBitmap {

    val bmp = ImageBitmap(w, h)
    val canvas = Canvas(bmp)

    CanvasDrawScope().draw(
        density = Density(1f, 1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(w.toFloat(), h.toFloat())
    ) {
        draw(this@draw.size)
    }

    return bmp
}