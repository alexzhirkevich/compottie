@file: Suppress("invisible_member", "invisible_reference")

/*
 * Copyright 2020 The Android Open Source Project
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

package io.github.alexzhirkevich.compottie.avp.xml

import io.github.alexzhirkevich.compottie.avp.AnimatedImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.DefaultGroupName
import androidx.compose.ui.graphics.vector.DefaultPathName
import io.github.alexzhirkevich.compottie.avp.EmptyPathAnimator
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Density
import io.github.alexzhirkevich.compottie.avp.animator.ColorData
import io.github.alexzhirkevich.compottie.avp.animator.FloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PathAnimator
import io.github.alexzhirkevich.compottie.avp.animator.StaticFloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.StaticPaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.StaticPathAnimator
import io.github.alexzhirkevich.compottie.avp.xml.BuildContext.Group
import org.jetbrains.compose.resources.vector.xmldom.Element
import org.jetbrains.compose.resources.vector.parseDp
import org.jetbrains.compose.resources.vector.parseFillType
import org.jetbrains.compose.resources.vector.parseColorValue
import org.jetbrains.compose.resources.vector.parseStrokeCap
import org.jetbrains.compose.resources.vector.parseStrokeJoin
import org.jetbrains.compose.resources.vector.parseTileMode
import org.jetbrains.compose.resources.vector.childrenSequence


//  Parsing logic is the same as in Android implementation
//  (compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/graphics/vector/compat/XmlVectorParser.kt)
//
//  Except there is no support for linking with external resources
//  (for example, we can't reference to color defined in another file)
//
//  Specification:
//  https://developer.android.com/reference/android/graphics/drawable/VectorDrawable

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private const val AAPT_NS = "http://schemas.android.com/aapt"

private class BuildContext {
    val currentGroups = mutableListOf<Group>()

    enum class Group {
        /**
         * Group that exists in xml file
         */
        Real,

        /**
         * Group that doesn't exist in xml file. We add it manually when we see <clip-path> node.
         * It will be automatically popped when the real group will be popped.
         */
        Virtual
    }
}

internal fun Element.toAnimatedImageVector(
    density: Density,
    animators : Map<String, Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>>,
): AnimatedImageVector {
    val context = BuildContext()
    val builder = AnimatedImageVector.Builder(
        defaultWidth = attributeOrNull(ANDROID_NS, "width").parseDp(density),
        defaultHeight = attributeOrNull(ANDROID_NS, "height").parseDp(density),
        viewportWidth = attributeOrNull(ANDROID_NS, "viewportWidth")?.toFloat() ?: 0f,
        viewportHeight = attributeOrNull(ANDROID_NS, "viewportHeight")?.toFloat() ?: 0f
    )
    parseVectorNodes(builder, animators, context)
    return builder.build()
}

private fun Element.parseVectorNodes(
    builder: AnimatedImageVector.Builder,
    animators : Map<String,Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>>,
    context: BuildContext
) {
    childrenSequence
        .filterIsInstance<Element>()
        .forEach {
            it.parseVectorNode(builder, animators, context)
        }
}

private fun Element.parseVectorNode(
    builder: AnimatedImageVector.Builder,
    animators : Map<String,Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>>,
    context: BuildContext
) {
    when (nodeName) {
        "path" -> parsePath(builder, animators)
        "clip-path" -> parseClipPath(builder, animators, context)
        "group" -> parseGroup(builder, animators, context)
    }
}

private fun Element.parseGroup(
    builder: AnimatedImageVector.Builder,
    animators : Map<String,Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>>,
    context: BuildContext
) {
    val name = attributeOrNull(ANDROID_NS, "name")

    val thisAnimators = animators[name].orEmpty()

    builder.addGroup(
        name = name ?: DefaultGroupName,
        rotate = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.Rotation,
        ),
        pivotX = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.PivotX,
        ),
        pivotY = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.PivotY,
        ),
        scaleX = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.ScaleX,
        ),
        scaleY = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.ScaleY,
        ),
        translationX = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.TranslationX,
        ),
        translationY = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.TranslationY,
        ),
        clipPathData = EmptyPathAnimator
    )
    context.currentGroups.add(Group.Real)

    parseVectorNodes(builder, animators, context)

    do {
        val removedGroup = context.currentGroups.removeLastOrNull()
        builder.clearGroup()
    } while (removedGroup == Group.Virtual)
}

private fun Element.parsePath(
    builder: AnimatedImageVector.Builder,
    animators : Map<String,Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>>,
) {
    val name = attributeOrNull(ANDROID_NS, "name")
    val thisAnimators = animators[name].orEmpty()

    builder.addPath(
        pathData = parsePathAnimator(thisAnimators, AnimatedVectorProperty.PathData),
        pathFillType = attributeOrNull(ANDROID_NS, "fillType")
            ?.let(::parseFillType) ?: PathFillType.NonZero,
        name = name ?: DefaultPathName,
        fill = parseColorAnimator(
            thisAnimators,
            AnimatedVectorProperty.FillColor
        ),
        fillAlpha = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.FillAlpha
        ),
        stroke =  parseColorAnimator(
            thisAnimators,
            AnimatedVectorProperty.StrokeColor
        ),
        strokeAlpha = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.StrokeAlpha
        ),
        strokeLineWidth = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.StrokeLineWidth
        ),
        strokeLineCap = attributeOrNull(ANDROID_NS, "strokeLineCap")
            ?.let(::parseStrokeCap) ?: StrokeCap.Butt,
        strokeLineJoin = attributeOrNull(ANDROID_NS, "strokeLineJoin")
            ?.let(::parseStrokeJoin) ?: StrokeJoin.Miter,
        strokeLineMiter = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.StrokeLineMiter
        ),
        trimPathStart = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.TrimPathStart
        ),
        trimPathEnd = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.TrimPathEnd
        ),
        trimPathOffset = parseFloatAnimator(
            thisAnimators,
            AnimatedVectorProperty.TrimPathOffset,
        )
    )
}

private fun Element.parseColorAnimator(
    animators: Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>,
    property: AnimatedVectorProperty<PaintAnimator>
) : PaintAnimator {
    return (animators[property] as? PaintAnimator?)
        ?: attributeOrNull(ANDROID_NS, property.propertyName)?.let {
            StaticPaintAnimator(ColorData.Solid(Color(parseColorValue(it))), property)
        }
        ?: apptAttr(ANDROID_NS, property.propertyName)?.let {
            when (attributeOrNull(ANDROID_NS, "type")) {
                "linear" -> parseLinearGradient()
                "radial" -> parseRadialGradient()
                "sweep" -> parseSweepGradient()
                else -> return@let property.defaultAnimator
            }.let {
                StaticPaintAnimator(it, property)
            }
        }
        ?: property.defaultAnimator
}


private fun Element.parseFloatAnimator(
    animators: Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>,
    property: AnimatedVectorProperty<FloatAnimator>
) : FloatAnimator {
    return (animators[property] as? FloatAnimator?)
        ?: attributeOrNull(ANDROID_NS, property.propertyName)?.toFloat()
            ?.let { StaticFloatAnimator(it, property) }
        ?: property.defaultAnimator
}

private fun Element.parsePathAnimator(
    animators: Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>,
    property: AnimatedVectorProperty<PathAnimator>
) : PathAnimator {
    return (animators[property] as? PathAnimator?)
        ?: attributeOrNull(ANDROID_NS, property.propertyName)?.let(::addPathNodes)?.let {
            StaticPathAnimator(it, property)
        } ?: property.defaultAnimator
}

private fun Element.parseClipPath(
    builder: AnimatedImageVector.Builder,
    animators : Map<String,Map<AnimatedVectorProperty<*>, ObjectAnimator<*,*>>>,
    context: BuildContext
) {
    val name = attributeOrNull(ANDROID_NS, "name")

    builder.addGroup(
        name = name ?: DefaultPathName,
        clipPathData = parsePathAnimator(
            animators[name].orEmpty(),
            AnimatedVectorProperty.PathData
        )
    )
    context.currentGroups.add(Group.Virtual)
}


private fun Element.parseElementBrush(): ColorData? =
    childrenSequence
        .filterIsInstance<Element>()
        .find { it.nodeName == "gradient" }
        ?.parseGradient()

private fun Element.parseGradient(): ColorData? {
    return when (attributeOrNull(ANDROID_NS, "type")) {
        "linear" -> parseLinearGradient()
        "radial" -> parseRadialGradient()
        "sweep" -> parseSweepGradient()
        else -> null
    }
}

private fun Element.parseLinearGradient() = ColorData.LinearGradient(
    colorStops = parseColorStops().toList(),
    start = Offset(
        attributeOrNull(ANDROID_NS, "startX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "startY")?.toFloat() ?: 0f
    ),
    end = Offset(
        attributeOrNull(ANDROID_NS, "endX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "endY")?.toFloat() ?: 0f
    ),
    tileMode = attributeOrNull(ANDROID_NS, "tileMode")?.let(::parseTileMode) ?: TileMode.Clamp
)

private fun Element.parseRadialGradient() = ColorData.RadialGradient(
    colorStops = parseColorStops(),
    center = Offset(
        attributeOrNull(ANDROID_NS, "centerX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "centerY")?.toFloat() ?: 0f
    ),
    radius = attributeOrNull(ANDROID_NS, "gradientRadius")?.toFloat() ?: 0f,
    tileMode = attributeOrNull(ANDROID_NS, "tileMode")?.let(::parseTileMode) ?: TileMode.Clamp
)

private fun Element.parseSweepGradient() = ColorData.SweepGradient(
    colorStops = parseColorStops(),
    center = Offset(
        attributeOrNull(ANDROID_NS, "centerX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "centerY")?.toFloat() ?: 0f,
    )
)

private fun Element.parseColorStops(): List<Pair<Float, Color>> {
    val items = childrenSequence
        .filterIsInstance<Element>()
        .filter { it.nodeName == "item" }
        .toList()

    val colorStops = items.mapIndexedNotNullTo(mutableListOf()) { index, item ->
        item.parseColorStop(defaultOffset = index.toFloat() / items.lastIndex.coerceAtLeast(1))
    }

    if (colorStops.isEmpty()) {
        val startColor = attributeOrNull(ANDROID_NS, "startColor")?.let(::parseColorValue)
        val centerColor = attributeOrNull(ANDROID_NS, "centerColor")?.let(::parseColorValue)
        val endColor = attributeOrNull(ANDROID_NS, "endColor")?.let(::parseColorValue)

        if (startColor != null) {
            colorStops.add(0f to Color(startColor))
        }
        if (centerColor != null) {
            colorStops.add(0.5f to Color(centerColor))
        }
        if (endColor != null) {
            colorStops.add(1f to Color(endColor))
        }
    }

    return colorStops
}

private fun Element.parseColorStop(defaultOffset: Float): Pair<Float, Color>? {
    val offset = attributeOrNull(ANDROID_NS, "offset")?.toFloat() ?: defaultOffset
    val color = attributeOrNull(ANDROID_NS, "color")?.let(::parseColorValue) ?: return null
    return offset to Color(color)
}

private fun Element.attributeOrNull(namespace: String, name: String): String? {
    val value = getAttributeNS(namespace, name)
    return value.ifBlank { null }
}

internal fun Element.androidAttributeOrNull(name: String): String? {
    val value = getAttributeNS(ANDROID_NS, name)
    return value.ifBlank { null }
}

internal fun Element.androidAttribute(name: String): String {
    return getAttributeNS(ANDROID_NS, name).also { check(it.isNotBlank()) }
}

/**
 * Attribute of an element can be represented as a separate child:
 *
 *  <path ...>
 *    <aapt:attr name="android:fillColor">
 *      <gradient ...
 *        ...
 *      </gradient>
 *    </aapt:attr>
 *  </path>
 *
 * instead of:
 *
 *  <path android:fillColor="red" ... />
 */
private fun Element.apptAttr(
    namespace: String,
    name: String
): Element? {
    val prefix = lookupPrefix(namespace)
    return childrenSequence
        .filterIsInstance<Element>()
        .find {
            it.namespaceURI == AAPT_NS && it.localName == "attr" &&
                it.getAttribute("name") == "$prefix:$name"
        }
}
