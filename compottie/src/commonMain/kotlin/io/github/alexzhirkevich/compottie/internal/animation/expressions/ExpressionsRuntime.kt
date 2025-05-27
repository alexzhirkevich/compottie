package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.color.JSHslToRgb
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.color.JSRgbToHsl
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.JSRadiansToDegrees
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.JsClamp
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.JsDegreesToRadians
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.JsFramesToTime
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.time.JsTimeToFrames
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.JSRuntime
import io.github.alexzhirkevich.keight.JavaScriptEngine
import io.github.alexzhirkevich.keight.Script
import io.github.alexzhirkevich.keight.ScriptEngine
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.VariableType
import io.github.alexzhirkevich.keight.findRoot
import io.github.alexzhirkevich.keight.js.JSPropertyAccessor
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.js
import kotlin.coroutines.CoroutineContext

internal class ExpressionsRuntime(
    context: CoroutineContext,
    val state : AnimationState
) : JSRuntime(
    context = context,
    isSuspendAllowed = false
) {

    private suspend fun init() {
        set("rgbToHsl".js(), JSRgbToHsl(), VariableType.Global)
        set("hslToRgb".js(), JSHslToRgb(), VariableType.Global)
        set("degreesToRadians".js(), JsDegreesToRadians(), VariableType.Global)
        set("radiansToDegrees".js(), JSRadiansToDegrees(), VariableType.Global)
        set("framesToTime".js(), JsFramesToTime(), VariableType.Global)
        set("timeToFrames".js(), JsTimeToFrames(), VariableType.Global)
        set("clamp".js(), JsClamp(), VariableType.Global)
        set(
            "time".js(),
            JSProperty { (state.time.inWholeMilliseconds / 1_000f).js() },
            VariableType.Global
        )

        set("thisComp".js(), JSProperty { state.thisComp }, VariableType.Const)
        set("thisLayer".js(), JSProperty { state.thisLayer }, VariableType.Const)
    }
}

internal class ExpressionsEngine(runtime: ExpressionsRuntime) : ScriptEngine() {

    private val js = JavaScriptEngine(runtime)

    override val runtime: ScriptRuntime
        get() = js.runtime

    override fun compile(script: String): Script {
        return js.compile("(function (){ $script; return \$bm_rt })()")
    }
}

internal val ScriptRuntime.state : AnimationState get() = (findRoot() as ExpressionsRuntime).state

internal fun JSProperty(get : suspend ScriptRuntime.() -> JsAny?) =
    JSPropertyAccessor.BackedField(Callable { get(this) })