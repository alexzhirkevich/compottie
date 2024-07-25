package lottiefiles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


private val DarkOpacity = Color(0xff7f7f7f)
private val LightOpacity = Color(0xffb2b2b2)

internal class OpacityGridPainter(
    enabled : Boolean = true,
     private val cellSize : Dp = 30.dp
) : Painter() {

     var enabled by mutableStateOf(enabled)

     override val intrinsicSize: Size get() = Size.Unspecified

     override fun DrawScope.onDraw() {
          if (enabled) {
               val sizePx = cellSize.toPx()
               val s = Size(sizePx, sizePx)
               repeat((size.width / sizePx).toInt() + 1) { i ->
                    repeat((size.height / sizePx).toInt() + 1) { j ->

                         drawRect(
                              color = if (i % 2 == j % 2) DarkOpacity else LightOpacity,
                              topLeft = Offset(i * sizePx, j * sizePx),
                              size = s
                         )
                    }
               }
          } else {
               drawRect(Color.White)
          }
     }
}