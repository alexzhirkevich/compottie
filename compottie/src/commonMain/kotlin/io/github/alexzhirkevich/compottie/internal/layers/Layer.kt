package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
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

    val blendMode : LottieBlendMode

    val transform : Transform

    val autoOrient : BooleanInt

    val matteMode : MatteMode?

    val matteParent : Int?

    val matteTarget : BooleanInt?

    val clazz : String?

    val htmlId : String?

    val collapseTransform : BooleanInt

    val masks : List<Mask>?

    var effects: List<LayerEffect>

    val effectsApplier : LayerEffectsApplier

    var painterProperties : PainterProperties?

    var namePath : String?

    fun onStart(composition: LottieComposition) {}
}

internal val Layer.isContainerLayer get()   =  name == "__container"

internal class PainterProperties(
    val assets: Map<String, LottieAsset> = emptyMap(),
    val fontFamilyResolver: FontFamily.Resolver? = null,
    val clipToDrawBounds : Boolean = true,
    val clipTextToBoundingBoxes : Boolean = false,
)



