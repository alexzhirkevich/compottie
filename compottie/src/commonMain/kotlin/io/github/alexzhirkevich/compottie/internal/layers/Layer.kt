package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicLayerProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JSGetLayerEffect
import io.github.alexzhirkevich.compottie.internal.animation.expressions.JSLayerToCompOrWorld
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsApplier
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.Undefined
import io.github.alexzhirkevich.keight.js.js
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("ty")
internal sealed interface Layer : DrawingContent, ExpressionHolder, Callable {

    val hidden: Boolean

    val index: Int?

    val parent: Int?

    val timeStretch: Float

    val inPoint: Float?

    val outPoint: Float?

    val startTime: Float?

    val blendMode: LottieBlendMode

    val transform: Transform

    val autoOrient: Boolean

    val matteMode: MatteMode?

    val matteParent: Int?

    val matteTarget: Boolean?

    val hasMask: Boolean?

    val masks: List<Mask>?

    var effects: List<LayerEffect>

    val effectsApplier: LayerEffectsApplier

    var resolvingPath: ResolvingPath?

    var parentLayer: Layer?

    val jsCache: MutableMap<String, JsAny?>

    val matteLayer: Layer?

    var comp : ExpressionComposition?

    fun setDynamicProperties(
        composition: DynamicCompositionProvider?,
        state: AnimationState
    ): DynamicLayerProvider?

    fun deepCopy(): Layer

    fun isHidden(state: AnimationState): Boolean

    fun isActive(state: AnimationState): Boolean

    override fun prepareExpressions(state: AnimationState) {
        transform.prepareExpressions(state)
        effects.fastForEach { it.prepareExpressions(state) }
        masks?.fastForEach { it.prepareExpressions(state) }
    }

    override suspend fun bind(
        thisArg: JsAny?,
        args: List<JsAny?>,
        runtime: ScriptRuntime
    ): Callable = this

    override suspend fun invoke(args: List<JsAny?>, runtime: ScriptRuntime): JsAny? {
        return when (args.getOrNull(0)?.toString()) {
            "1" -> if (isActive(runtime.state)) 1.js else 0.js
            "6" -> if (this is BaseCompositionLayer) timeRemapping else Undefined
            "7" -> name?.js
            "8" -> parentLayer
            "9" -> blendMode.type.js
            "10" -> matteLayer
            "Effects", "ADBE Effect Parade" -> jsCache.getOrPut("Effects") {
                Callable {
                    val name = it.getOrNull(0)?.toString() ?: return@Callable null
                    effects.fastFirstOrNull { it.name == name || it.matchName == name }
                }
            } 
            else -> Undefined
        }
    }

    override suspend fun keys(
        runtime: ScriptRuntime,
        excludeSymbols: Boolean,
        excludeNonEnumerables: Boolean
    ): List<JsAny?> = listOf(
        "index".js,
        "name".js,
        "source".js,
        "inPoint".js,
        "outPoint".js,
        "startTime".js,
        "source".js,
        "active".js,
        "enabled".js,
        "hasParent".js,
        "parent".js,
        "transform".js,
        "rotation".js,
        "position".js,
        "opacity".js,
        "timeRemap".js,
        "effect".js,
        "eff".js,
        "toComp".js,
        "fromComp".js,
        "toWorld".js,
        "fromWorld".js,
    )

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property?.toString()) {
            "index" -> index?.js ?: Undefined
            "name" -> name?.js ?: Undefined
            "source" -> comp
            "inPoint" -> inPoint?.div(runtime.state.composition.frameRate)?.js ?: Undefined
            "outPoint" -> outPoint?.div(runtime.state.composition.frameRate)?.js ?: Undefined
            "startTime" -> startTime?.div(runtime.state.composition.frameRate)?.js ?: Undefined
            "active" -> isActive(runtime.state).js
            "enabled" -> isHidden(runtime.state).not().js
            "hasParent" -> (parentLayer != null).js
            "parent" -> parentLayer ?: Undefined
            "transform" -> transform
            "rotation" -> transform.rotation
            "position" -> transform.position
            "opacity" -> transform.opacity
            "effect", "eff" -> jsCache.getOrPut("effect") { JSGetLayerEffect(this) } 
            "mask" -> jsCache.getOrPut("mask") {
                Callable {
                    val name = it[0]?.toKotlin(this)?.toString() ?: return@Callable Undefined
                    masks?.fastFirstOrNull { it.name == name }
                }
            } 
            "toComp" -> jsCache.getOrPut("toComp") {
                JSLayerToCompOrWorld(layer = this, reverse = false, toComp = true)
            }
            "fromComp" -> jsCache.getOrPut("fromComp") {
                JSLayerToCompOrWorld(layer = this, reverse = true, toComp = true)
            } 
            "toWorld" -> jsCache.getOrPut("toWorld") {
                JSLayerToCompOrWorld(layer = this, reverse = false, toComp = false)
            } 
            "fromWorld" -> jsCache.getOrPut("fromWorld") {
                JSLayerToCompOrWorld(layer = this, reverse = true, toComp = false)
            } 
            else -> super.get(property, runtime)
        }
    }
}

internal fun Layer.totalTransformMatrix(state : AnimationState, toComp : Boolean = false) : Matrix {

    val matrix = transform.matrix(state)
    var layer = parentLayer

    while (layer != null) {
        matrix.preConcat(layer.transform.matrix(state))
        if (toComp && layer is PrecompositionLayer){
            break
        }
        layer = layer.parentLayer
    }

    return matrix
}

@JvmInline
internal value class ResolvingPath private constructor(val path : String) {
    fun resolve(child : String) = ResolvingPath("$path/$child")

    companion object {
        val root = ResolvingPath("")
    }
}

internal fun ResolvingPath.resolveOrNull(child: String?) : ResolvingPath? =
    if (child != null) resolve(child) else null

internal val Layer.isContainerLayer get()  =  name == CONTAINER_NAME



