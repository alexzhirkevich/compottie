package io.github.alexzhirkevich.compottie.internal.assets

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import io.github.alexzhirkevich.compottie.internal.platform.fromBytes
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable(with = AssetSerializer::class)
internal sealed interface LottieAsset {

    val id: String

    @Serializable
    data object UnsupportedAsset : LottieAsset {
        override val id: String get() = ""
    }
}



internal class AssetSerializer : JsonContentPolymorphicSerializer<LottieAsset>(LottieAsset::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<LottieAsset> {
        return when {
            "layers" in element.jsonObject.keys -> PrecompositionAsset.serializer()
            "p" in element.jsonObject.keys -> ImageAsset.serializer()
            else -> LottieAsset.UnsupportedAsset.serializer()
        }
    }
}

