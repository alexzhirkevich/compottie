package io.github.alexzhirkevich.compottie.internal.layers

import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicLayerProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsApplier
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.jvm.JvmInline

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("ty")
internal sealed interface Layer : DrawingContent {

    val is3d: BooleanInt

    val hidden: Boolean

    val index : Int?

    val parent : Int?

    val timeStretch : Float

    val inPoint : Float?

    val outPoint : Float?

    val startTime : Float?

    val blendMode : LottieBlendMode

    val transform : Transform

    val autoOrient : BooleanInt

    val matteMode : MatteMode?

    val matteParent : Int?

    val matteTarget : BooleanInt?

    val clazz : String?

    val htmlId : String?

    val collapseTransform : BooleanInt

    val hasMask : Boolean?

    val masks : List<Mask>?

    var effects: List<LayerEffect>

    val effectsApplier : LayerEffectsApplier

    var resolvingPath : ResolvingPath?

    fun setDynamicProperties(composition: DynamicCompositionProvider?, state: AnimationState): DynamicLayerProvider?

    fun deepCopy() : Layer
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



