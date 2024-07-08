package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.animation.Vec2

internal fun unresolvedProperty(property : String, obj : String) : Nothing =
    error("Unresolved property '$property' for $obj")


internal operator fun Any.get(index : Int) : Any {
    return when (this){
        is Map<*,*> -> {
            (this as Map<Int, *>).get(index)
        }
        is List<*> -> this.get(index)
        is Array<*> -> this.get(index)
        is Vec2 -> when (index){
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds [0,1]")
        }
        is Color -> when(index){
            0 -> red
            1 -> green
            2 -> blue
            3 -> alpha
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds [0,3]")
        }
        else -> error("Can't get value by index from $this")
    }!!
}