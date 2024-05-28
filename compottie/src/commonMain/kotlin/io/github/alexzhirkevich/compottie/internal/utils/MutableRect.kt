package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Rect

fun MutableRect.union(other : MutableRect){
    if ((left < right) && (top < bottom)) {
        if ((this.left < this.right) && (this.top < this.bottom)) {
            if (this.left > left) this.left = left;
            if (this.top > top) this.top = top;
            if (this.right < right) this.right = right;
            if (this.bottom < bottom) this.bottom = bottom;
        } else {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}

fun MutableRect.set(other : MutableRect){
    set(left = other.left, top = other.top, right = other.right, bottom = other.bottom)
}

fun MutableRect.set(other : Rect){
    set(left = other.left, top = other.top, right = other.right, bottom = other.bottom)
}

