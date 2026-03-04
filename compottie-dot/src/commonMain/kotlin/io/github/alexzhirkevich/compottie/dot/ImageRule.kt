package io.github.alexzhirkevich.compottie.dot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Image")
internal class ImageRule(
    override val id: String,
    override val value: ImageValue? = null,
) : ThemeRule<ImageValue>

@Serializable
internal class ImageValue(
    val id : String? = null,
    val width : Int? = null,
    val height : Int? = null,
    val url : String? = null,
)
