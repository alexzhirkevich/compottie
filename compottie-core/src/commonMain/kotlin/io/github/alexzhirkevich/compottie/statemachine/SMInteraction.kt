package io.github.alexzhirkevich.compottie.statemachine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SMInteraction {

    public val actions : List<SMAction>

    val layerName : String? get() = null

    @Serializable
    @SerialName("PointerUp")
    public class PointerUp(
        override val actions: List<SMAction>,
        override val layerName : String? = null,
    ) : SMInteraction

    @Serializable
    @SerialName("PointerDown")
    public class PointerDown(
        override val actions: List<SMAction>,
        override val layerName : String? = null,
    ) : SMInteraction

    @Serializable
    @SerialName("PointerEnter")
    public class PointerEnter(
        override val actions: List<SMAction>,
        override val layerName : String? = null,
    ) : SMInteraction

    @Serializable
    @SerialName("PointerMove")
    public class PointerMove(
        override val actions: List<SMAction>,
        override val layerName : String? = null,
    ) : SMInteraction

    @Serializable
    @SerialName("PointerExit")
    public class PointerExit(
        override val actions: List<SMAction>,
        override val layerName : String? = null,
    ) : SMInteraction

    @Serializable
    @SerialName("Click")
    public class Click(
        override val actions: List<SMAction>,
        override val layerName : String? = null,
    ) : SMInteraction

    @Serializable
    @SerialName("OnComplete")
    public class OnComplete(
        override val actions: List<SMAction>,
        public val stateName : String,
    ) : SMInteraction

    @Serializable
    @SerialName("OnLoopComplete")
    public class OnLoopComplete(
        override val actions: List<SMAction>,
        public val stateName : String,
    ) : SMInteraction
}