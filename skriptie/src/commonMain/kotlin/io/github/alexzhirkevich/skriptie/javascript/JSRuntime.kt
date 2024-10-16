package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.DefaultScriptIO
import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.ScriptIO
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.ecmascript.ESNumber
import io.github.alexzhirkevich.skriptie.ecmascript.ESObject
import io.github.alexzhirkevich.skriptie.ecmascript.ESRuntime
import io.github.alexzhirkevich.skriptie.ecmascript.init


public open class JSRuntime(
    io: ScriptIO = DefaultScriptIO
) : ESRuntime(io = io), LangContext by JSLangContext {

    init {
        recreate()
    }

    override fun reset() {
        super.reset()
        recreate()
    }

    private fun recreate() {
        set("Math", JsMath(), VariableType.Const)
        set("console", JsConsole(), VariableType.Const)

        init {
            val number = get("Number") as ESNumber
            val globalThis = get("globalThis") as ESObject

            globalThis.set("parseInt", number.parseInt)
            globalThis.set("parseFloat", number.parseFloat)
            globalThis.set("isFinite", number.isFinite)
            globalThis.set("isNan", number.isNan)
            globalThis.set("isInteger", number.isInteger)
            globalThis.set("isSafeInteger", number.isSafeInteger)
        }
    }
}

