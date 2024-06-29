package io.github.alexzhirkevich.compottie.dynamic

interface DynamicShapeLayer: DynamicLayer {

    /**
     * Configure generic dynamic shape.
     * */
    fun shape(
        vararg path: String,
        builder: DynamicShape.() -> Unit
    )

    /**
     * Shortcut useful if you want to configure multiple shapes in the same group
     *
     * Example:
     *
     * ```
     * group("Group 1") {
     *     ellipse("Ellipse 4") { }
     *     fill("Fill 1") { }
     * }
     * ```
     * Is alternative for
     * ```
     * ellipse("Group 1", "Ellipse 4") { }
     * fill("Group 1", "Fill 1") { }
     *```
     * */
    fun group(vararg path: String, builder: DynamicShapeLayer.() -> Unit)

    /**
     * Configure dynamic stroke.
     * */
    fun stroke(
        vararg path: String,
        builder: DynamicStroke.() -> Unit
    )

    /**
     * Configure dynamic fill.
     * */
    fun fill(
        vararg path: String,
        builder: DynamicFill.() -> Unit
    )

    /**
     * Configure dynamic ellipse.
     * */
    fun ellipse(
        vararg path: String,
        builder: DynamicEllipse.() -> Unit
    )

    /**
     * Configure dynamic rect.
     * */
    fun rect(
        vararg path: String,
        builder: DynamicRect.() -> Unit
    )

    /**
     * Configure dynamic rect.
     * */
    fun polystar(
        vararg path: String,
        builder: DynamicPolystar.() -> Unit
    )
}