package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.isIdentity
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal val IdentityMatrix = Matrix()

internal fun Matrix.preTranslate(x : Float, y : Float) {
    if (x.absoluteValue < Float.MIN_VALUE && y.absoluteValue< Float.MIN_VALUE) {
        return
    }
//    preConcat(tempMatrixTransform.apply {
//        fastReset()
//        translate(x, y)
//    })
    return translate(x, y)
}

internal fun Matrix.preConcat(other : Matrix) {

    if (other.isIdentity()) {
        return
    }

    if (isIdentity()){
        fastSetFrom(other)
        return
    }

    val temp = Matrix()
    temp.fastSetFrom(other)
    temp.timesAssign(this)
    fastSetFrom(temp)

//    timesAssign(other)
}

internal fun Matrix.fastReset() {
    fastSetFrom(IdentityMatrix)
}

internal fun Matrix.fastSetFrom(other : Matrix){
    other.values.copyInto(values)
}

internal fun Matrix.setValues(values : FloatArray){
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

internal fun Matrix.preRotate(degree : Float) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
//    preConcat(tempMatrixTransform.apply {
//        fastReset()
//        rotateZ(degree)
//    })
//
    return rotateZ(degree)
}

internal fun Matrix.preRotateX(degree : Float) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
    preConcat(Matrix().apply {
        rotateX(degree)
    })
}

internal fun Matrix.preRotateY(degree : Float) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
    preConcat(Matrix().apply {
        rotateY(degree)
    })
}
internal fun Matrix.preRotateZ(degree : Float) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
    preConcat(Matrix().apply {
        rotateZ(degree)
    })
}

internal fun Matrix.preScale(x : Float, y : Float) {
    if (x == 1f && y == 1f) {
        return
    }
//    preConcat(tempMatrixTransform.apply {
//        fastReset()
//        scale(x, y)
//    })
   scale(x,y)
}

