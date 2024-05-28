package io.github.alexzhirkevich.compottie.internal.schema.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal class ShapeProperty(

    @SerialName("c")
    val closed : Boolean,

    /**
     * Bezier curve In points. Array of 2 dimensional arrays
     * */
    @SerialName("i")
    val inPoints : List<FloatArray>,

    /**
     * Bezier curve Out points. Array of 2 dimensional arrays.
     * */
    @SerialName("o")
    val outPoints : List<FloatArray>,

    /**
     * Bezier curve Vertices. Array of 2 dimensional arrays.
     * */
    @SerialName("v")
    val vertices : List<FloatArray>
)