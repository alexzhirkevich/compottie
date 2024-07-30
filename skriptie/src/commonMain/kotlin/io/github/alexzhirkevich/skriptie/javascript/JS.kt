package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Script
import io.github.alexzhirkevich.skriptie.ScriptEngine
import io.github.alexzhirkevich.skriptie.ecmascript.EcmascriptInterpreter

public class JS(
    override val context: JSScriptContext = JSScriptContext(),
    private val globalContext: JsGlobalContext = JsGlobalContext(),
    private val extensionContext : JsExtensionContext = JsExtensionContext(),
) : ScriptEngine<JSScriptContext> {

    override fun compile(script: String): Script<JSScriptContext> {
        return EcmascriptInterpreter(
            expr = script,
            scriptContext = context,
            globalContext = globalContext,
            extensionContext = extensionContext
        ).interpret()
    }

    override fun reset() {
        context.reset()
    }
}