package io.github.alexzhirkevich.compottie.internal.schema.shapes

import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("tm")
internal class Trim(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("s")
    val start : AnimatedValue,

    @SerialName("e")
    val end : AnimatedValue,

    @SerialName("o")
    val offset : AnimatedValue,
) : Shape