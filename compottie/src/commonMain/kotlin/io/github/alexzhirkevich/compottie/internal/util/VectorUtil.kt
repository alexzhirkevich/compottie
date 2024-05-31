package io.github.alexzhirkevich.compottie.internal.schema.util

import androidx.compose.ui.geometry.Offset

internal fun FloatArray.toOffset() = Offset(this[0], this[1])