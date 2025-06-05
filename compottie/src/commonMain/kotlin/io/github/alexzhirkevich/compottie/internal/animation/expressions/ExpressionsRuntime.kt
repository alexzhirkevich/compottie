package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.timeSeconds
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
import io.github.alexzhirkevich.keight.js.Undefined
import io.github.alexzhirkevich.keight.js.js
import kotlinx.atomicfu.atomic
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

private val easeIn = CubicBezierEasing(0.333f, 0f, 0.833f, 0.833f)
private val easeOut = CubicBezierEasing(0.167f, 0.167f, 0.667f, 1f)
private val easeInOut = CubicBezierEasing(0.33f, 0f, 0.667f, 1f)

internal class ExpressionsRuntime(
    context: CoroutineContext,
    val state : AnimationState
) : JSRuntime(
    context = context,
    isSuspendAllowed = false
) {

    private var initialized by atomic(false)

    suspend fun init() {
        if (initialized)
            return

        initialized = true

        set("time".js(), JSProperty { state.timeSeconds.js() }, VariableType.Const)

//        set("value".js(), JSProperty { state.thisProperty?.raw(state)?.toJs() }, VariableType.Const)

        set("thisComp".js(), JSProperty { state.thisComp }, VariableType.Const)
        set("thisLayer".js(), JSProperty { state.thisLayer }, VariableType.Const)
        set("thisProp".js(), JSProperty { state.thisProperty }, VariableType.Const)

        set("add".js(), Callable { sum(it[0], it[1]) }, VariableType.Const)
        set("sum".js(), Callable { sum(it[0], it[1]) }, VariableType.Const)
        set("\$bm_sum".js(), Callable { sum(it[0], it[1]) }, VariableType.Const)
        set("sub".js(), Callable { sub(it[0], it[1]) }, VariableType.Const)
        set("\$bm_sub".js(), Callable { sub(it[0], it[1]) }, VariableType.Const)
        set("mul".js(), Callable { mul(it[0], it[1]) }, VariableType.Const)
        set("\$bm_mul".js(), Callable { mul(it[0], it[1]) }, VariableType.Const)
        set("div".js(), Callable { div(it[0], it[1]) }, VariableType.Const)
        set("\$bm_div".js(), Callable { div(it[0], it[1]) }, VariableType.Const)
        set("mod".js(), Callable { mod(it[0], it[1]) }, VariableType.Const)
        set("clamp".js(), JsClamp(), VariableType.Const)
        set("dot".js(), JsDot(), VariableType.Const)
        set("length".js(), JsLength(), VariableType.Const)
        set("normalize".js(), JsNormalize(), VariableType.Const)

        set("degreesToRadians".js(), JsDegreesToRadians(), VariableType.Const)
        set("radiansToDegrees".js(), JSRadiansToDegrees(), VariableType.Const)
        set("rgbToHsl".js(), JSRgbToHsl(), VariableType.Const)
        set("hslToRgb".js(), JSHslToRgb(), VariableType.Const)
        set("hexToRgb".js(), JSHexToRgb(), VariableType.Const)
        set("framesToTime".js(), JsFramesToTime(), VariableType.Const)
        set("timeToFrames".js(), JsTimeToFrames(), VariableType.Const)

        set("random".js(), JSRandomNumber(false), VariableType.Const)
        set("gaussRandom".js(), JSRandomNumber(true), VariableType.Const)
        set("setRandom".js(), JSSeedRandom(), VariableType.Const)
        set("noise".js(), JSNoise(), VariableType.Const)

        set("linear".js(), JSInterpolate(LinearEasing), VariableType.Const)
        set("ease".js(), JSInterpolate(easeInOut), VariableType.Const)
        set("easeIn".js(), JSInterpolate(easeIn), VariableType.Const)
        set("easeOut".js(), JSInterpolate(easeOut), VariableType.Const)
    }

    override suspend fun contains(property: JsAny?): Boolean {
        return super.contains(property)
                || state.thisProperty?.contains(property, this) == true
                || state.thisLayer.contains(property, this)
                || state.thisComp.contains(property, this)
    }

    override suspend fun get(property: JsAny?): JsAny? {
        state.thisProperty?.let {
            it.get(property, this).also { if (it != Undefined) return it }
        }
        state.thisLayer.get(property, this).also { if (it != Undefined) return it }
        state.thisComp.get(property, this).also { if (it != Undefined) return it }
        super.get(property).also { if (it != Undefined) return it }

        return Undefined
    }

    override suspend fun sum(a: JsAny?, b: JsAny?): JsAny? {
        return when {
            a is List<*> && b is List<*> -> {
                a as List<JsAny?>
                b as List<JsAny?>
                List(max(a.size, b.size)) {
                    sum(
                        a.getOrElse(it) { 0.js() },
                        b.getOrElse(it) { 0.js() },
                    )
                }.js()
            }
            else -> super.sum(a, b)
        }
    }

    override suspend fun sub(a: JsAny?, b: JsAny?): JsAny? {
        return when {
            a is List<*> && b is List<*> -> {
                a as List<JsAny?>
                b as List<JsAny?>
                List(max(a.size, b.size)) {
                    sub(
                        a.getOrElse(it) { 0.js() },
                        b.getOrElse(it) { 0.js() },
                    )
                }.js()
            }
            else -> super.sub(a, b)
        }
    }


    override suspend fun div(a: JsAny?, b: JsAny?): JsAny? {
        return when {
            a is List<*> -> (a as List<JsAny?>).fastMap { div(it, b) }.js()

            else -> super.div(a, b)
        }
    }

    override suspend fun mul(a: JsAny?, b: JsAny?): JsAny? {
        return when {
            a is List<*> -> (a as List<JsAny?>).fastMap { mul(it, b) }.js()
            else -> super.mul(a, b)
        }
    }
}

internal class ExpressionsEngine(
    override val runtime: ExpressionsRuntime
) : ScriptEngine() {

    private val js = JavaScriptEngine(runtime)


    override fun compile(script: String): Script {
        return js.compile("(function (){ $script; return \$bm_rt })()")
    }
}

internal val ScriptRuntime.state : AnimationState get() = (findRoot() as ExpressionsRuntime).state

internal fun JSProperty(get : suspend ScriptRuntime.() -> JsAny?) =
    JSPropertyAccessor.BackedField(Callable { get(this) })