@file: Suppress("invisible_member", "invisible_reference")
package io.github.alexzhirkevich.compottie.avp.xml

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import io.github.alexzhirkevich.compottie.avp.animator.ColorData
import io.github.alexzhirkevich.compottie.avp.animator.DynamicFloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.DynamicPaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.DynamicPathAnimator
import io.github.alexzhirkevich.compottie.avp.animator.FloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PathAnimator
import org.jetbrains.compose.resources.vector.xmldom.Element
import org.jetbrains.compose.resources.vector.childrenSequence
import org.jetbrains.compose.resources.vector.xmldom.MalformedXMLException

internal fun Element.parseObjectAnimators() : List<ObjectAnimator<*, *>> {
    if (this.nodeName == "set") {
        return childrenSequence
                .filterIsInstance<Element>()
                .map(Element::toObjectAnimator)
                .toList()
    }
    if (this.nodeName == "objectAnimator") {
        return listOf(toObjectAnimator())
    }

    throw MalformedXMLException("Unable to parse animator XML")
}

private fun Element.toObjectAnimator() : ObjectAnimator<*, *> {
    val propertyName = androidAttribute("propertyName")
    val valueType = androidAttributeOrNull("valueType")
    val duration = androidAttribute("duration").toFloat()
    val valueFrom = androidAttribute("valueFrom")
    val valueTo = androidAttribute("valueTo")
    val property = AnimatedVectorProperty.forName(androidAttribute("propertyName"))

    return getValueType(valueType, valueFrom).toAnimator(
        duration,
        valueFrom,
        valueTo,
        property = property
    )
}

private fun getValueType(type: String?, value : String) : ValueType<*> {
    return when (type){
        "intType", "floatType" -> ValueType.Number
        "colorType" -> ValueType.Color
        "pathType" -> ValueType.Path
        else -> when {
            value.matches(colorRegex) -> ValueType.Color
            value.matches(floatRegex) -> ValueType.Number
            else -> ValueType.Path
        }
    }
}

private val colorRegex = Regex("^#(?:[0-9a-fA-F]{3,4}){1,2}\$")
private val floatRegex = Regex("^-?\\d*(\\.\\d+)?$")
private val intRegex = Regex("/^[-+]?\\d+\$/")

private sealed interface ValueType<T>  {

    fun parse(value : String) : T

    object Color : ValueType<ColorData> {
        override fun parse(value: String): ColorData {
            TODO()
        }
    }

    object Number : ValueType<Float> {
        override fun parse(value: String) = value.toFloat()
    }

    object Path : ValueType<List<PathNode>> {
        override fun parse(value: String): List<PathNode>  = PathParser().parsePathString(value).toNodes()
    }
}

@Suppress("unchecked_cast")
private fun ValueType<*>.toAnimator(
    duration : Float,
    from : String,
    to : String,
    property: AnimatedVectorProperty<*>
) : ObjectAnimator<*, *> {

    val fromValue = parse(from)
    val toValue = parse(to)

    return when (this) {
        ValueType.Color -> DynamicPaintAnimator(
            duration = duration,
            valueFrom = fromValue as ColorData,
            valueTo = toValue as ColorData,
            delay = 0f,
            easing = LinearEasing,
            property = property as AnimatedVectorProperty<PaintAnimator>
        )

        ValueType.Number -> DynamicFloatAnimator(
            duration = duration,
            valueFrom = fromValue as Float,
            valueTo = toValue as Float,
            delay = 0f,
            easing = LinearEasing,
            property = property as AnimatedVectorProperty<FloatAnimator>
        )

        ValueType.Path -> DynamicPathAnimator(
            duration = duration,
            valueFrom = fromValue as List<PathNode>,
            valueTo = toValue as List<PathNode>,
            delay = 0f,
            easing = LinearEasing,
            property = property as AnimatedVectorProperty<PathAnimator>
        )
    }
}