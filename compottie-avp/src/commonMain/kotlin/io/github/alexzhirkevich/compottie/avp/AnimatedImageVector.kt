package io.github.alexzhirkevich.compottie.avp/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.DefaultGroupName
import androidx.compose.ui.graphics.vector.DefaultPathName
import androidx.compose.ui.graphics.vector.DefaultStrokeLineCap
import androidx.compose.ui.graphics.vector.DefaultStrokeLineJoin
import androidx.compose.ui.unit.Dp
import io.github.alexzhirkevich.compottie.avp.animator.FloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PathAnimator
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Vector graphics object that is generated as a result of [ImageVector.Builder]
 * It can be composed and rendered by passing it as an argument to [rememberVectorPainter]
 */
@Immutable
internal class AnimatedImageVector internal constructor(

    /**
     * Name of the Vector asset
     */
    val name: String,

    /**
     * Intrinsic width of the vector asset in [Dp]
     */
    val defaultWidth: Dp,

    /**
     * Intrinsic height of the vector asset in [Dp]
     */
    val defaultHeight: Dp,

    /**
     *  Used to define the width of the viewport space. Viewport is basically the virtual canvas
     *  where the paths are drawn on.
     */
    val viewportWidth: Float,

    /**
     * Used to define the height of the viewport space. Viewport is basically the virtual canvas
     * where the paths are drawn on.
     */
    val viewportHeight: Float,

    /**
     * Root group of the vector asset that contains all the child groups and paths
     */
    val root: AnimatedVectorGroup,

    /**
     * Optional tint color to be applied to the vector graphic
     */
    val tintColor: Color,

    /**
     * Blend mode used to apply [tintColor]
     */
    val tintBlendMode: BlendMode,

    /**
     * Determines if the vector asset should automatically be mirrored for right to left locales
     */
    val autoMirror: Boolean,

    /**
     * Identifier used to disambiguate between different ImageVector instances in a more efficient
     * manner than equality. This can be used as a key for caching instances of ImageVectors.
     */
    internal val genId: Int = generateImageVectorId(),
) {
    /**
     * Builder used to construct a Vector graphic tree.
     * This is useful for caching the result of expensive operations used to construct
     * a vector graphic for compose.
     * For example, the vector graphic could be serialized and downloaded from a server and represented
     * internally in a ImageVector before it is composed through [rememberVectorPainter]
     * The generated ImageVector is recommended to be memoized across composition calls to avoid
     * doing redundant work
     */
    @Suppress("MissingGetterMatchingBuilder")
    class Builder(

        /**
         * Name of the vector asset
         */
        private val name: String = DefaultGroupName,

        /**
         * Intrinsic width of the Vector in [Dp]
         */
        private val defaultWidth: Dp,

        /**
         * Intrinsic height of the Vector in [Dp]
         */
        private val defaultHeight: Dp,

        /**
         *  Used to define the width of the viewport space. Viewport is basically the virtual canvas
         *  where the paths are drawn on.
         */
        private val viewportWidth: Float,

        /**
         * Used to define the height of the viewport space. Viewport is basically the virtual canvas
         * where the paths are drawn on.
         */
        private val viewportHeight: Float,

        /**
         * Optional color used to tint the entire vector image
         */
        private val tintColor: Color = Color.Unspecified,

        /**
         * Blend mode used to apply the tint color
         */
        private val tintBlendMode: BlendMode = BlendMode.SrcIn,

        /**
         * Determines if the vector asset should automatically be mirrored for right to left locales
         */
        private val autoMirror: Boolean = false
    ) {

        private val nodes = ArrayList<GroupParams>()

        private var root = GroupParams()
        private var isConsumed = false

        private val currentGroup: GroupParams
            get() = nodes.peek()

        init {
            nodes.push(root)
        }

        /**
         * Create a new group and push it to the front of the stack of ImageVector nodes
         *
         * @param name the name of the group
         * @param rotate the rotation of the group in degrees
         * @param pivotX the x coordinate of the pivot point to rotate or scale the group
         * @param pivotY the y coordinate of the pivot point to rotate or scale the group
         * @param scaleX the scale factor in the X-axis to apply to the group
         * @param scaleY the scale factor in the Y-axis to apply to the group
         * @param translationX the translation in virtual pixels to apply along the x-axis
         * @param translationY the translation in virtual pixels to apply along the y-axis
         * @param clipPathData the path information used to clip the content within the group
         *
         * @return This ImageVector.Builder instance as a convenience for chaining calls
         */
        @Suppress("MissingGetterMatchingBuilder")
        fun addGroup(
            name: String = DefaultGroupName,
            rotate: FloatAnimator = DefaultRotationAnimator,
            pivotX: FloatAnimator = DefaultPivotXAnimator,
            pivotY: FloatAnimator = DefaultPivotYAnimator,
            scaleX: FloatAnimator = DefaultScaleXAnimator,
            scaleY: FloatAnimator = DefaultScaleYAnimator,
            translationX: FloatAnimator = DefaultTranslationXAnimator,
            translationY: FloatAnimator = DefaultTranslationYAnimator,
            clipPathData: PathAnimator = EmptyPathAnimator
        ): Builder {
            ensureNotConsumed()
            val group = GroupParams(
                name,
                rotate,
                pivotX,
                pivotY,
                scaleX,
                scaleY,
                translationX,
                translationY,
                clipPathData
            )
            nodes.push(group)
            return this
        }

        /**
         * Pops the topmost VectorGroup from this ImageVector.Builder. This is used to indicate
         * that no additional ImageVector nodes will be added to the current VectorGroup
         * @return This ImageVector.Builder instance as a convenience for chaining calls
         */
        fun clearGroup(): Builder {
            ensureNotConsumed()
            val popped = nodes.pop()
            currentGroup.children.add(popped.asVectorGroup())
            return this
        }

        /**
         * Add a path to the ImageVector graphic. This represents a leaf node in the ImageVector graphics
         * tree structure
         *
         * @param pathData path information to render the shape of the path
         * @param pathFillType rule to determine how the interior of the path is to be calculated
         * @param name the name of the path
         * @param fill specifies the [Brush] used to fill the path
         * @param fillAlpha the alpha to fill the path
         * @param stroke specifies the [Brush] used to fill the stroke
         * @param strokeAlpha the alpha to stroke the path
         * @param strokeLineWidth the width of the line to stroke the path
         * @param strokeLineCap specifies the linecap for a stroked path
         * @param strokeLineJoin specifies the linejoin for a stroked path
         * @param strokeLineMiter specifies the miter limit for a stroked path
         * @param trimPathStart specifies the fraction of the path to trim from the start in the
         * range from 0 to 1. Values outside the range will wrap around the length of the path.
         * Default is 0.
         * @param trimPathStart specifies the fraction of the path to trim from the end in the
         * range from 0 to 1. Values outside the range will wrap around the length of the path.
         * Default is 1.
         * @param trimPathOffset specifies the fraction to shift the path trim region in the range
         * from 0 to 1. Values outside the range will wrap around the length of the path. Default is 0.
         *
         * @return This ImageVector.Builder instance as a convenience for chaining calls
         */
        @Suppress("MissingGetterMatchingBuilder")
        fun addPath(
            pathData: PathAnimator,
            pathFillType: PathFillType = DefaultFillType,
            name: String = DefaultPathName,
            fill: PaintAnimator? = null,
            fillAlpha: FloatAnimator,
            stroke: PaintAnimator? = null,
            strokeAlpha: FloatAnimator,
            strokeLineWidth: FloatAnimator,
            strokeLineCap: StrokeCap = DefaultStrokeLineCap,
            strokeLineJoin: StrokeJoin = DefaultStrokeLineJoin,
            strokeLineMiter: FloatAnimator,
            trimPathStart: FloatAnimator,
            trimPathEnd: FloatAnimator,
            trimPathOffset: FloatAnimator
        ): Builder {
            ensureNotConsumed()
            currentGroup.children.add(
                AnimatedVectorPath(
                    name,
                    pathData,
                    pathFillType,
                    fill,
                    fillAlpha,
                    stroke,
                    strokeAlpha,
                    strokeLineWidth,
                    strokeLineCap,
                    strokeLineJoin,
                    strokeLineMiter,
                    trimPathStart,
                    trimPathEnd,
                    trimPathOffset
                )
            )
            return this
        }

        /**
         * Construct a ImageVector. This concludes the creation process of a ImageVector graphic
         * This builder cannot be re-used to create additional ImageVector instances
         * @return The newly created ImageVector instance
         */
        fun build(): AnimatedImageVector {
            ensureNotConsumed()
            // pop all groups except for the root
            while (nodes.size > 1) {
                clearGroup()
            }

            val vectorImage = AnimatedImageVector(
                name,
                defaultWidth,
                defaultHeight,
                viewportWidth,
                viewportHeight,
                root.asVectorGroup(),
                tintColor,
                tintBlendMode,
                autoMirror
            )

            isConsumed = true

            return vectorImage
        }

        /**
         * Throws IllegalStateException if the ImageVector.Builder has already been consumed
         */
        private fun ensureNotConsumed() {
            check(!isConsumed) {
                "ImageVector.Builder is single use, create a new instance " +
                        "to create a new ImageVector"
            }
        }

        /**
         * Helper method to create an immutable VectorGroup object
         * from an set of GroupParams which represent a group
         * that is in the middle of being constructed
         */
        private fun GroupParams.asVectorGroup(): AnimatedVectorGroup =
            AnimatedVectorGroup(
                name,
                rotate,
                pivotX,
                pivotY,
                scaleX,
                scaleY,
                translationX,
                translationY,
                clipPathData,
                children
            )

        /**
         * Internal helper class to help assist with in progress creation of
         * a vector group before creating the immutable result
         */
        private class GroupParams(
            var name: String = DefaultGroupName,
            var rotate: FloatAnimator = DefaultRotationAnimator,
            var pivotX: FloatAnimator = DefaultPivotXAnimator,
            var pivotY: FloatAnimator = DefaultPivotXAnimator,
            var scaleX: FloatAnimator = DefaultScaleXAnimator,
            var scaleY: FloatAnimator = DefaultScaleYAnimator,
            var translationX: FloatAnimator = DefaultTranslationXAnimator,
            var translationY: FloatAnimator = DefaultTranslationXAnimator,
            var clipPathData: PathAnimator = EmptyPathAnimator,
            var children: MutableList<AnimatedVectorNode> = mutableListOf()
        )
    }

    companion object {
        private var imageVectorCount = 0

        private val lock = SynchronizedObject()

        internal fun generateImageVectorId(): Int {
            synchronized(lock) {
                return imageVectorCount++
            }
        }
    }
}

internal sealed class AnimatedVectorNode

/**
 * Defines a group of paths or subgroups, plus transformation information.
 * The transformations are defined in the same coordinates as the viewport.
 * The transformations are applied in the order of scale, rotate then translate.
 *
 * This is constructed as part of the result of [ImageVector.Builder] construction
 */
@Immutable
internal class AnimatedVectorGroup internal constructor(
    /**
     * Name of the corresponding group
     */
    val name: String = DefaultGroupName,

    /**
     * Rotation of the group in degrees
     */
    val rotation: FloatAnimator,

    /**
     * X coordinate of the pivot point to rotate or scale the group
     */
    val pivotX: FloatAnimator,

    /**
     * Y coordinate of the pivot point to rotate or scale the group
     */
    val pivotY: FloatAnimator,

    /**
     * Scale factor in the X-axis to apply to the group
     */
    val scaleX: FloatAnimator,

    /**
     * Scale factor in the Y-axis to apply to the group
     */
    val scaleY: FloatAnimator,

    /**
     * Translation in virtual pixels to apply along the x-axis
     */
    val translationX: FloatAnimator,

    /**
     * Translation in virtual pixels to apply along the y-axis
     */
    val translationY: FloatAnimator,

    /**
     * Path information used to clip the content within the group
     */
    val clipPathData: PathAnimator,

    /**
     * Child Vector nodes that are part of this group, this can contain
     * paths or other groups
     */
    val children: List<AnimatedVectorNode> = emptyList()
) : AnimatedVectorNode()

/**
 * Leaf node of a Vector graphics tree. This specifies a path shape and parameters
 * to color and style the shape itself
 *
 * This is constructed as part of the result of [ImageVector.Builder] construction
 */
@Immutable
internal class AnimatedVectorPath internal constructor(
    /**
     * Name of the corresponding path
     */
    val name: String = DefaultPathName,

    /**
     * Path information to render the shape of the path
     */
    val pathData: PathAnimator,

    /**
     * Rule to determine how the interior of the path is to be calculated
     */
    val pathFillType: PathFillType,

    /**
     *  Specifies the color or gradient used to fill the path
     */
    val fill: PaintAnimator? = null,

    /**
     * Opacity to fill the path
     */
    val fillAlpha: FloatAnimator,

    /**
     * Specifies the color or gradient used to fill the stroke
     */
    val stroke: PaintAnimator? = null,

    /**
     * Opacity to stroke the path
     */
    val strokeAlpha: FloatAnimator,

    /**
     * Width of the line to stroke the path
     */
    val strokeLineWidth: FloatAnimator,

    /**
     * Specifies the linecap for a stroked path, either butt, round, or square. The default is butt.
     */
    val strokeLineCap: StrokeCap = DefaultStrokeLineCap,

    /**
     * Specifies the linejoin for a stroked path, either miter, round or bevel. The default is miter
     */
    val strokeLineJoin: StrokeJoin = DefaultStrokeLineJoin,

    /**
     * Specifies the miter limit for a stroked path, the default is 4
     */
    val strokeLineMiter: FloatAnimator,

    /**
     * Specifies the fraction of the path to trim from the start, in the range from 0 to 1.
     * The default is 0.
     */
    val trimPathStart: FloatAnimator,

    /**
     * Specifies the fraction of the path to trim from the end, in the range from 0 to 1.
     * The default is 1.
     */
    val trimPathEnd: FloatAnimator,

    /**
     * Specifies the offset of the trim region (allows showed region to include the start and end),
     * in the range from 0 to 1. The default is 0.
     */
    val trimPathOffset: FloatAnimator
) : AnimatedVectorNode()

private fun <T> ArrayList<T>.push(value: T): Boolean = add(value)

private fun <T> ArrayList<T>.pop(): T = this.removeAt(size - 1)

private fun <T> ArrayList<T>.peek(): T = this[size - 1]
