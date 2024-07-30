package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.compottie.internal.animation.expressions.BaseScriptContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ScriptContext
import io.github.alexzhirkevich.skriptie.ecmascript.GlobalContext

public open class JSScriptContext(
    override val globalContext: GlobalContext<ScriptContext> = JsGlobalContext()
) : BaseScriptContext() {
}