package io.github.alexzhirkevich.compottie.internal.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.isIdentity
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val tempMatrixConcat = Matrix()
private val tempMatrixTransform = Matrix()


internal val IdentityMatrix = Matrix()

private val InvSqrt2Offset = Offset(
    1/sqrt(2f),
    1/sqrt(2f),
)
internal val Matrix.scale: Float get() {
    val p1 = map(Offset.Zero)
    val p2 = map(InvSqrt2Offset)

    // hypot can result in float errors like 1.00000010
    // that cause some problems like invalid stroke dash offset
    return (hypot(p2.x - p1.x, p2.y - p1.y) * 1000).roundToInt() / 1000f
}

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

    tempMatrixConcat.fastSetFrom(other)
    tempMatrixConcat.timesAssign(this)
    fastSetFrom(tempMatrixConcat)

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
    preConcat(tempMatrixTransform.apply {
        fastReset()
        rotateX(degree)
    })
}

internal fun Matrix.preRotateY(degree : Float) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
    preConcat(tempMatrixTransform.apply {
        fastReset()
        rotateY(degree)
    })
}
internal fun Matrix.preRotateZ(degree : Float) {
    if (degree.absoluteValue < Float.MIN_VALUE) {
        return
    }
    preConcat(tempMatrixTransform.apply {
        fastReset()
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

