package io.github.alexzhirkevich.compottie.dynamic

interface DynamicShapeLayer: DynamicLayer {

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
     *
     * ```
     * ellipse("Group 1", "Ellipse 4") { }
     * fill("Group 1", "Fill 1") { }
     *```
     * */
    fun group(name: String, builder: DynamicShapeLayer.() -> Unit)

    /**
     * Configure dynamic stroke.
     *
     * Example:
     *
     * ```
     * stroke("Group 1", "Stroke 2") {
     *     width { 5f }
     *     color { if (isDark) Color.White else Color.Black }
     * }
     * ```
     * */
    fun stroke(
        vararg path: String,
        builder: DynamicStroke.() -> Unit
    )

    /**
     * Configure dynamic fill.
     *
     * Example:
     *
     * ```
     * fill("Group 1", "Stroke 2") {
     *     color { if (isDark) Color.White else Color.Black }
     * }
     * ```
     * */
    fun fill(
        vararg path: String,
        builder: DynamicFill.() -> Unit
    )

    fun ellipse(
        vararg path: String,
        builder: DynamicEllipse.() -> Unit
    )

    fun rect(
        vararg path: String,
        builder: DynamicRect.() -> Unit
    )
}