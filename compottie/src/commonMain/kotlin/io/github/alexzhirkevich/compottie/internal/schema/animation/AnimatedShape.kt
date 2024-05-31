package io.github.alexzhirkevich.compottie.internal.schema.animation

import io.github.alexzhirkevich.compottie.internal.schema.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.schema.helpers.CubicCurveData
import io.github.alexzhirkevich.compottie.internal.schema.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.schema.helpers.toShapeData
import io.github.alexzhirkevich.compottie.internal.schema.util.toOffset
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedShape : Animated<ShapeData>, Indexable {

    @SerialName("0")
    class Default(
        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("k")
        val bezier : Bezier
    ) : AnimatedShape {

        private val shapeData = bezier.toShapeData()

        override fun interpolated(frame: Int): ShapeData {
            return shapeData
        }
    }

    @SerialName("1")
    @Serializable
    class Keyframed(
        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("k")
        val keyframes : List<ShapeKeyframe>
    ) : AnimatedShape {

        @Transient
        private var tempShapeData = ShapeData()


        override fun interpolated(frame: Int): ShapeData {
            TODO("Not yet implemented")
        }
    }
}