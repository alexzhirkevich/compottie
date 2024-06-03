package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
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

    val startTime : Int?

    val name : String?

    var painterProperties : PainterProperties?
}

internal val Layer.isContainerLayer get()   =  name == "__container"

internal class PainterProperties(
    val composition : LottieComposition,

    val assets: Map<String, LottieAsset> = emptyMap(),

    val maintainOriginalImageBounds: Boolean = false,

    val clipTextToBoundingBoxes : Boolean = false,

    val fontFamilyResolver: FontFamily.Resolver? = null
)



