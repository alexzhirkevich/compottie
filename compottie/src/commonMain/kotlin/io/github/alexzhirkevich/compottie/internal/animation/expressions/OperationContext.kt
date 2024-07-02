package io.github.alexzhirkevich.compottie.internal.animation.expressions

internal interface OperationContext {
    
    fun evaluate(op : String, args : List<Operation>) : Operation
}