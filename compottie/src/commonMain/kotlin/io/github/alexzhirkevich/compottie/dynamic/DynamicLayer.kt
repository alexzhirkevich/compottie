package io.github.alexzhirkevich.compottie.dynamic

import kotlin.reflect.KClass

sealed interface DynamicLayer {

    fun transform(builder: DynamicTransform.() -> Unit)

    sealed interface Shape : DynamicLayer {

        /**
         * Shortcut useful if you want to configure multiple shapes in the same group
         *
         * Example:
         *
         * ```
         * group("Group 1") {
         *     ellipse("Ellipse 4") { }
         *     fill(DynamicFill.Solid, "Fill 1") { }
         * }
         * ```
         * Is alternative for
         *
         * ```
         * ellipse("Group 1", "Ellipse 4") { }
         * fill<DynamicFill.Solid>("Group 1", "Fill 1") { }
         *```
         * */
        fun group(name: String, shape: Shape.() -> Unit)

    }
}


/**
 * Configure dynamic stroke.
 *
 * Fill type must be the same as actual animation fill type.
 * You can't configure gradient stroke if the animation is using solid stroke.
 *
 * Example:
 *
 * ```
 * stroke<DynamicStroke.Solid>("Group 1", "Stroke 2") {
 *     width { 5f }
 *     color { if (isDark) Color.White else Color.Black }
 * }
 * ```
 * */
inline fun <reified T : DynamicStroke> DynamicLayer.Shape.stroke(
    vararg path: String,
    crossinline builder: T.() -> Unit
) = strokeImpl<T>(*path, builder = builder)

/**
 * Configure dynamic fill.
 *
 * Fill type must be the same as actual animation fill type.
 * You can't configure gradient fill if the animation is using solid fill.
 *
 * Example:
 *
 * ```
 * fill<DynamicFill.Solid>("Group 1", "Stroke 2") {
 *     color { if (isDark) Color.White else Color.Black }
 * }
 * ```
 * */
inline fun <reified T : DynamicStroke> DynamicLayer.Shape.fill(
    vararg path: String,
    crossinline builder: T.() -> Unit
) = strokeImpl<T>(*path, builder = builder)
