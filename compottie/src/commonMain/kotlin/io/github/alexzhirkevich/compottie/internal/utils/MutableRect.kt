package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Rect
import kotlin.math.max
import kotlin.math.min

internal fun MutableRect.intersect(other: MutableRect) =
    intersect(
        left = other.left,
        top = other.top,
        right = other.right,
        bottom = other.bottom
    )
internal fun MutableRect.overlaps(other: MutableRect): Boolean {
    if (right <= other.left || other.right <= left)
        return false
    if (bottom <= other.top || other.bottom <= top)
        return false
    return true
}

internal fun MutableRect.union(other : MutableRect) {

    if ((other.left >= other.right) || (other.top >= other.bottom)) {
        return
    }

    if ((left < right) && (top < bottom)) {
        set(other)
        return
    }

    left = min(left, other.left)
    top = min(top, other.top)
    right = max(right, other.top)
    bottom = max(right, other.bottom)
}

internal fun MutableRect.set(other : MutableRect){
    set(left = other.left, top = other.top, right = other.right, bottom = other.bottom)
}

internal fun MutableRect.set(other : Rect){
    set(left = other.left, top = other.top, right = other.right, bottom = other.bottom)
}

