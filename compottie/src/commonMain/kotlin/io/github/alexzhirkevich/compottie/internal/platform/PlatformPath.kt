package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.shapes.TrimPathShape
import io.github.alexzhirkevich.compottie.internal.utils.floorMod
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal expect fun ExtendedPathMeasure() : ExtendedPathMeasure

internal interface ExtendedPathMeasure : PathMeasure {
    fun nextContour() : Boolean
}

internal fun Path.set(other : Path){
    reset()
    addPath(other)
}

//internal expect fun Path.set(other : Path)

internal expect fun Path.addPath(path: Path, matrix: Matrix) : Path

