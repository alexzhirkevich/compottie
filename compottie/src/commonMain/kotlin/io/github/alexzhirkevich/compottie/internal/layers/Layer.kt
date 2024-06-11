package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("ty")
internal sealed interface Layer {

    val is3d: BooleanInt

    val hidden: Boolean

    val index : Int?

    val parent : Int?

    val timeStretch : Float

    val inPoint : Float?

    val outPoint : Float?

    val name : String?

    var painterProperties : PainterProperties?

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

    val effects: List<LayerEffect>

    fun applyBlurEffectIfNeeded(paint: Paint, state: AnimationState, lastBlurRadius : Float?) : Float
}

internal val Layer.isContainerLayer get()   =  name == "__container"

internal class PainterProperties(
    val composition : LottieComposition,

    val assets: Map<String, LottieAsset> = emptyMap(),

    val clipTextToBoundingBoxes : Boolean = false,

    val fontFamilyResolver: FontFamily.Resolver? = null
)



