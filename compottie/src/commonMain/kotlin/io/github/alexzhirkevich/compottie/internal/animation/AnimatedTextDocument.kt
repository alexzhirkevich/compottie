package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.dynamic.DynamicTextLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextDocument
import io.github.alexzhirkevich.compottie.internal.utils.toOffset
import io.github.alexzhirkevich.compottie.internal.utils.toSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.collections.ArrayList

@Serializable
internal class AnimatedTextDocument(

    @SerialName("k")
    override val keyframes : List<TextDocumentKeyframe>,

    @SerialName("x")
    val expression : String? = null,

    @SerialName("ix")
    override val index: Int? = null,

    @SerialName("sid")
    val slotID : String? = null
) : KeyframeAnimation<TextDocument, TextDocumentKeyframe> {

    private val document = TextDocument()

    @Transient
    private var dynamic :DynamicTextLayerProvider? = null

    private val fillColorList by lazy {
        ArrayList<Float>(4)
    }

    private val strokeColorList by lazy {
        ArrayList<Float>(4)
    }

    private val sizeList by lazy {
        ArrayList<Float>(2)
    }

    private val positionList by lazy {
        ArrayList<Float>(2)
    }

    fun dynamic(provider : DynamicTextLayerProvider?){
        dynamic = provider
    }

    @Transient
    private val delegate = BaseKeyframeAnimation(
        index = index,
        keyframes = keyframes,
        emptyValue = document,
        map = { s, e, p ->
            //TODO: lerp properties?
            if (p != 1.0f) s else e
        }
    )

    override fun interpolated(state: AnimationState): TextDocument {
        val interp = delegate.interpolated(state)

        return document.apply {
            fontFamily = interp.fontFamily
            fillColor = dynamic?.fillColor?.let {
                it.derive(interp.fillColor?.toColor() ?: Color.Unspecified, state).let {
                    fillColorList.fill(it)
                }
            } ?: interp.fillColor
            strokeColor = dynamic?.strokeColor?.let {
                it.derive(interp.strokeColor?.toColor() ?: Color.Unspecified, state).let {
                    strokeColorList.fill(it)
                }
            } ?: interp.strokeColor
            strokeWidth = dynamic?.strokeWidth.derive(interp.strokeWidth, state)
            strokeOverFill = dynamic?.strokeOverFill.derive(interp.strokeOverFill, state)
            fontSize = dynamic?.fontSize.derive(interp.fontSize, state)
            lineHeight = dynamic?.lineHeight.derive(interp.lineHeight, state)
            wrapSize = dynamic?.wrapSize?.let {
                it.derive(interp.wrapSize?.toSize() ?: Size.Unspecified, state).let {
                    sizeList[0] = it.width
                    sizeList[1] = it.height
                    sizeList
                }
            } ?: interp.wrapSize
            wrapPosition = dynamic?.wrapPosition?.let {
                it.derive(interp.wrapPosition?.toOffset() ?: Offset.Unspecified, state).let {
                    positionList[0] = it.x
                    positionList[1] = it.y
                    positionList
                }
            } ?: interp.wrapPosition
            text = dynamic?.text.derive(interp.text.orEmpty(), state)
            textJustify = dynamic?.textJustify.derive(interp.textJustify, state)
            textTracking = dynamic?.tracking.derive(interp.textTracking ?: 0f, state)
            baselineShift =
                dynamic?.baselineShift.derive(interp.baselineShift ?: 0f, state)
            textCaps = interp.textCaps
        }
    }

    fun copy() = AnimatedTextDocument(
        keyframes = keyframes,
        expression = expression,
        slotID = slotID,
        index = index
    )
}

private fun MutableList<Float>.fill(color : Color) : MutableList<Float> {
    this[0] = color.red
    this[1] = color.green
    this[2] = color.blue
    this[3] = color.alpha

    return this
}