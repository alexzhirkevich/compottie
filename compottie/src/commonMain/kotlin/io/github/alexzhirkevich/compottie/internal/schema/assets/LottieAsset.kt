package io.github.alexzhirkevich.compottie.internal.schema.assets

import androidx.compose.ui.graphics.ImageBitmap
import io.github.alexzhirkevich.compottie.internal.platform.fromBytes
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.PolymorphicSerializer
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
    val name: String?

    @Serializable
    class ImageAsset(

        @SerialName("id")
        override val id: String,

        @SerialName("p")
        override val fileName: String,

        @SerialName("u")
        override val path: String ="",


        @SerialName("sid")
        override val slotId: String? = null,

        @SerialName("nm")
        override val name: String? = null,

        @SerialName("e")
        override val embedded: BooleanInt = BooleanInt.No,

        @SerialName("w")
        private val w: Int? = null,

        @SerialName("h")
        private val h: Int? = null,
    ) : LottieFileAsset {

        val width : Int get() = w ?: bitmap?.width ?: 0

        val height : Int get() = h ?: bitmap?.height ?: 0

        @OptIn(ExperimentalEncodingApi::class)
        @Transient
        val bitmap: ImageBitmap? = fileName
            .takeIf(String::isBase64Data::get)
            ?.substringAfter("base64,")
            ?.trim()
            ?.let { ImageBitmap.fromBytes(Base64.decode(it)) }
    }

    @Serializable
    class EmptyAsset(
        @SerialName("id")
        override val id: String,

        @SerialName("nm")
        override val name: String? = null,
    ) : LottieAsset
}

private val String.isBase64Data : Boolean get() =
    (startsWith("data:") && indexOf("base64,") > 0)

internal class AssetSerializer : JsonContentPolymorphicSerializer<LottieAsset>(LottieAsset::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<LottieAsset> {
        return when {
            "p" in element.jsonObject.keys -> LottieAsset.ImageAsset.serializer()
            else -> LottieAsset.EmptyAsset.serializer()
        }
    }
}

