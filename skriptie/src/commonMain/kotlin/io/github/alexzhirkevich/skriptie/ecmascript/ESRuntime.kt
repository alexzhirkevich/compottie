package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.DefaultRuntime
import io.github.alexzhirkevich.skriptie.VariableType

public abstract class ESRuntime() : DefaultRuntime() {

    init {
        init()
    }

    override fun reset() {
        super.reset()
        init()
    }

    private fun init(){
        set("Number", ESNumber(), VariableType.Const)
        set("Infinity", Double.POSITIVE_INFINITY, VariableType.Const)
    }
}