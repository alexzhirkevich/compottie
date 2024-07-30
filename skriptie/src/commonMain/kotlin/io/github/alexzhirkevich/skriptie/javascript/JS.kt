package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.compottie.internal.animation.expressions.EcmascriptInterpreter
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Script
import io.github.alexzhirkevich.skriptie.ecmascript.ScriptEngine

public class JS(
    override val context: JSScriptContext = JSScriptContext(),
    private val globalContext: JsGlobalContext = JsGlobalContext(),
    private val extensionContext : JsExtensionContext = JsExtensionContext()
) : ScriptEngine<JSScriptContext> {

    override fun compile(script: String): Script<JSScriptContext> {
        val expression = EcmascriptInterpreter(
            expr = script,
            scriptContext = context,
            globalContext = globalContext,
            extensionContext = extensionContext
        ).interpret()

        return Script { expression.invoke(it.context) }
    }

    override fun reset() {
        context.reset()
    }
}