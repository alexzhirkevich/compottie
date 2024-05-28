package io.github.alexzhirkevich.compottie.internal.schema.layers

import androidx.compose.ui.graphics.Canvas
import io.github.alexzhirkevich.compottie.internal.schema.effects.Effect
import io.github.alexzhirkevich.compottie.internal.schema.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.schema.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.properties.MatteMode
import io.github.alexzhirkevich.compottie.internal.schema.shapes.BaseCanvasShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.CanvasShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Shape
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("4")
internal class ShapeLayer(

    @SerialName("ks")
    override val transform : Transform = Transform(),

    @SerialName("ao")
    override val autoOrient : BooleanInt = BooleanInt.No,

    @SerialName("ddd")
    override val is3d: BooleanInt = BooleanInt.No,

    @SerialName("ind")
    override val index : Int? = null,

    @SerialName("bm")
    override val blendMode: LottieBlendMode = LottieBlendMode.Normal,

    @SerialName("cl")
    override val clazz : String? = null,

    @SerialName("ln")
    override val htmlId : String? = null,

    @SerialName("ip")
    override val inPoint : Float? = null,

    @SerialName("op")
    override val outPoint : Float? = null,

    @SerialName("st")
    override val startTime : Int? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("ef")
    val effect : List<Effect> = emptyList(),

    @SerialName("sr")
    override val stretch : Float = 1f,

    @SerialName("parent")
    override val parent : Int? = null,

    @SerialName("shapes")
    override val shapes : List<Shape> = emptyList(),

    @SerialName("tt")
    override val matteMode : MatteMode? = null,

    @SerialName("tp")
    override val matteParent : Int? = null,

    @SerialName("td")
    override val matteTarget : BooleanInt? = null,

    @SerialName("hd")
    override val hidden: Boolean = false,

    override val collapseTransform: BooleanInt = BooleanInt.No
) : VisualLayer, CanvasShape by BaseCanvasShape(
    root = true, hidden = hidden, shapes = shapes, matchName = name
)
