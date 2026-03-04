package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.isIdentity
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
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

internal fun Matrix.preRotateX(degree: Float) = preRotateOnAxis(degree, 1, 2)
internal fun Matrix.preRotateY(degree: Float) = preRotateOnAxis(degree, 2, 0)
internal fun Matrix.preRotateZ(degree: Float) = preRotateOnAxis(degree, 0, 1)

private fun Matrix.preRotateOnAxis(degree: Float, rowA: Int, rowB: Int) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
    val rad = degree.toDouble() * (PI / 180.0)
    val c = cos(rad).toFloat()
    val s = sin(rad).toFloat()
    val v = values
    for (col in 0..3) {
        val i = col * 4
        val a = v[i + rowA]
        val b = v[i + rowB]
        v[i + rowA] = c * a - s * b
        v[i + rowB] = s * a + c * b
    }
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

