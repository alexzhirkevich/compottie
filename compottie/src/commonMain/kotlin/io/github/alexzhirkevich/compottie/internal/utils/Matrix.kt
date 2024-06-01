package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.graphics.Matrix

private val tempMatrixConcat = Matrix()
private val tempMatrixTransform = Matrix()

fun Matrix.preTranslate(x : Float, y : Float) {

    preConcat(tempMatrixTransform.apply {
        reset()
        translate(x, y)
    })
//    return translate(x, y)
}

fun Matrix.preConcat(other : Matrix) {
    tempMatrixConcat.setFrom(other)
    tempMatrixConcat.timesAssign(this)
    this.setFrom(tempMatrixConcat)
//    timesAssign(other)
}

fun Matrix.setValues(values : FloatArray){
    this.values[Matrix.ScaleX] = values[0]
    this.values[Matrix.SkewX] = values[1]
    this.values[Matrix.TranslateX] = values[2]
    this.values[Matrix.SkewY] = values[3]
    this.values[Matrix.ScaleY] = values[4]
    this.values[Matrix.TranslateY] = values[5]
    this.values[Matrix.Perspective0] = values[6]
    this.values[Matrix.Perspective1] = values[7]
    this.values[Matrix.Perspective2] = values[8]
}

fun Matrix.preRotate(degree : Float) {
    preConcat(tempMatrixTransform.apply {
        reset()
        rotateZ(degree)
    })

    return rotateZ(degree)
}

fun Matrix.preScale(x : Float, y : Float) {
    preConcat(tempMatrixTransform.apply {
        reset()
        scale(x, y)
    })
//    return scale(x,y)
}

