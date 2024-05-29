package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.graphics.Matrix
import org.jetbrains.skia.Matrix44

fun Matrix.preTranslate(x : Float, y : Float) {
    preConcat(Matrix().apply {  translate(x,y) })
//    return translate(x, y)
}


private val tempMatrix = Matrix()

fun Matrix.preConcat(other : Matrix) {
    tempMatrix.setFrom(other)
    tempMatrix.timesAssign(this)
    this.setFrom(tempMatrix)
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