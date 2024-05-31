package io.github.alexzhirkevich.compottie.internal.schema.layers

import io.github.alexzhirkevich.compottie.internal.schema.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.schema.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.schema.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.helpers.MatteMode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("ty")
internal sealed interface VisualLayer : Layer {

    val transform : Transform?

    val autoOrient : BooleanInt

    val matteMode : MatteMode?

    val matteParent : Int?

    val matteTarget : BooleanInt?

    val blendMode: LottieBlendMode

    val clazz : String?

    val htmlId : String?

    val collapseTransform : BooleanInt
}



