package io.github.alexzhirkevich.compottie.internal.schema.layers

import io.github.alexzhirkevich.compottie.internal.services.LottieServiceLocator
import io.github.alexzhirkevich.compottie.internal.schema.helpers.BooleanInt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("ty")
internal sealed interface Layer {

    val is3d: BooleanInt

    val hidden: Boolean

    val index : Int?

    val parent : Int?

    val stretch : Float

    val inPoint : Float?

    val outPoint : Float?

    val startTime : Int?

    val name : String?

    var serviceLocator : LottieServiceLocator?

    var density : Float
}



