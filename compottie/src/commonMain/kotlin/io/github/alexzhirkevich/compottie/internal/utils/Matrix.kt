package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.graphics.Matrix

fun Matrix.preTranslate(x : Float, y : Float) {
    return translate(x, y)
}

fun Matrix.preConcat(other : Matrix) {
    return timesAssign(other)
}

fun Matrix.setValues(values : FloatArray){
    values.copyInto(this.values)
}

fun Matrix.preRotate(degree : Float) {
    return rotateZ(degree)
}

fun Matrix.preScale(x : Float, y : Float) {
    return scale(x,y)
}