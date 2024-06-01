package io.github.alexzhirkevich.compottie.internal.layers

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

    var composition : LottieComposition


    var density : Float

    var assets: Map<String, LottieAsset>

    var maintainOriginalImageBounds: Boolean
}

internal val Layer.isContainerLayer get()   =  name == "__container"



