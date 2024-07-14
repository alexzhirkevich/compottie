package io.github.alexzhirkevich.compottie.avp.xml

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import io.github.alexzhirkevich.compottie.avp.animator.ColorData
import io.github.alexzhirkevich.compottie.avp.animator.DynamicFloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.DynamicPaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.DynamicPathAnimator
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import org.jetbrains.compose.resources.vector.xmldom.Element
import org.jetbrains.compose.resources.vector.xmldom.MalformedXMLException

internal fun Element.parseAnimation() : List<ObjectAnimator<*, *>> {
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

    return getValueType(valueType, valueFrom).toAnimator(
        duration,
        valueFrom,
        valueTo
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
private fun <T> ValueType<T>.toAnimator(
    duration : Float,
    from : String,
    to : String
) : ObjectAnimator<*, *> {

    val fromValue = parse(from)
    val toValue = parse(to)

    return when (this) {
        ValueType.Color -> DynamicPaintAnimator(
            duration = duration,
            valueFrom = fromValue as ColorData,
            valueTo = toValue as ColorData,
            startOffset = 0f,
            interpolator = LinearEasing
        )

        ValueType.Number -> DynamicFloatAnimator(
            duration = duration,
            valueFrom = fromValue as Float,
            valueTo = toValue as Float,
            startOffset = 0f,
            interpolator = LinearEasing
        )

        ValueType.Path -> DynamicPathAnimator(
            duration = duration,
            valueFrom = fromValue as List<PathNode>,
            valueTo = toValue as List<PathNode>,
            startOffset = 0f,
            interpolator = LinearEasing
        )
    }
}