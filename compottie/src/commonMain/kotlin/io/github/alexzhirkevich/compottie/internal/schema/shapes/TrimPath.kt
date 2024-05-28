package io.github.alexzhirkevich.compottie.internal.schema.shapes

import io.github.alexzhirkevich.compottie.internal.schema.properties.TrimPathType
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("tr")
internal class TrimPath(

    @SerialName("n")
    override val name: String? = null,

    @SerialName("mn")
    override val matchName: String?,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("s")
    val start : Value,

    @SerialName("e")
    val end : Value,

    @SerialName("o")
    val offset : Value,

    @SerialName("m")
    val type : TrimPathType = TrimPathType.Simultaneously
) : Shape {
}