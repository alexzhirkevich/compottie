package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.EcmascriptContext
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.javascript.math.JsMath

public open class JSScriptContext : EcmascriptContext() {

    init {
        recreate()
    }

    override fun reset() {
        super.reset()
        recreate()
    }

    private fun recreate(){
        variables["Math"] = VariableType.Const to JsMath()
        variables["Infinity"] = VariableType.Const to Double.POSITIVE_INFINITY
    }
}