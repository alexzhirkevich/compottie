package io.github.alexzhirkevich.compottie.internal.graphics

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.schema.layers.ShapeLayer


internal fun ShapeLayer.draw(scope: DrawScope, time : Int) {
    scope.drawIntoCanvas { canvas ->
        canvas.withSave {
            canvas.concat(transform.matrix(time))
            drawIntoCanvas(
                canvas = canvas,
                time = time
            )
        }
    }
}