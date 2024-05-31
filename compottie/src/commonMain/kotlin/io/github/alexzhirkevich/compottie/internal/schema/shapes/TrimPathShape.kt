package io.github.alexzhirkevich.compottie.internal.schema.shapes

import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.schema.helpers.TrimPathType
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Serializable
@SerialName("tm")
internal class TrimPathShape(

    @SerialName("n")
    override val name: String? = null,

    @SerialName("mn")
    override val matchName: String?,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("s")
    val start : AnimatedValue,

    @SerialName("e")
    val end : AnimatedValue,

    @SerialName("o")
    val offset : AnimatedValue,

    @SerialName("m")
    val type : TrimPathType = TrimPathType.Simultaneously
) : Shape {
    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }
}


@OptIn(ExperimentalContracts::class)
internal fun Content.isSimultaneousTrimPath() : Boolean {
    contract {
        returns(true) implies (this@isSimultaneousTrimPath is TrimPathShape)
    }

    return this is TrimPathShape && type == TrimPathType.Simultaneously
}

@OptIn(ExperimentalContracts::class)
internal fun Content.isIndividualTrimPath() : Boolean {
    contract {
        returns(true) implies (this@isIndividualTrimPath is TrimPathShape)
    }

    return this is TrimPathShape && type == TrimPathType.Simultaneously
}