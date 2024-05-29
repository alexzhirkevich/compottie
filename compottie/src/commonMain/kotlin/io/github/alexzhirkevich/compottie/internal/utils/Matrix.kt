package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.graphics.Matrix

private val tempMatrix = Matrix()


fun Matrix.preTranslate(x : Float, y : Float) {

    preConcat(tempMatrix.apply {
        reset()
        translate(x, y)
    })
//    return translate(x, y)
}

fun Matrix.preConcat(other : Matrix) {
    tempMatrix.setFrom(other)
    tempMatrix.timesAssign(this)
    this.setFrom(tempMatrix)
//    timesAssign(other)
}

fun Matrix.setValues(values : FloatArray){
    values.copyInto(this.values)
}

fun Matrix.preRotate(degree : Float) {
    preConcat(tempMatrix.apply {
        reset()
        rotateZ(degree)
    })

//    return rotateZ(degree)
}

fun Matrix.preScale(x : Float, y : Float) {
    preConcat(tempMatrix.apply {
        reset()
        scale(x, y)
    })
//    return scale(x,y)
}