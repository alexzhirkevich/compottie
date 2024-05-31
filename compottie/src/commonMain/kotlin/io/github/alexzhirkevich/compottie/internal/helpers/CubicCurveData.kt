package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.geometry.Offset

internal class CubicCurveData(
    var controlPoint1: Offset = Offset.Zero,
    var controlPoint2: Offset = Offset.Zero,
    var vertex: Offset = Offset.Zero
)