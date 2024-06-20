package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.animation.toColor
import io.github.alexzhirkevich.compottie.internal.assets.CharacterData
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextData
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextDocument
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextJustify
import io.github.alexzhirkevich.compottie.internal.platform.addCodePoint
import io.github.alexzhirkevich.compottie.internal.platform.charCount
import io.github.alexzhirkevich.compottie.internal.platform.codePointAt
import io.github.alexzhirkevich.compottie.internal.platform.isModifier
import io.github.alexzhirkevich.compottie.internal.util.toOffset
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import io.github.alexzhirkevich.compottie.internal.utils.preTranslate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("5")
internal class TextLayer(

    @SerialName("ks")
    override val transform: Transform = Transform(),

    @SerialName("ddd")
    override val is3d: BooleanInt = BooleanInt.No,

    @SerialName("ind")
    override val index: Int? = null,

    @SerialName("ip")
    override val inPoint: Float? = null,

    @SerialName("op")
    override val outPoint: Float? = null,

    @SerialName("nm")
    override val name: String? = null,

    @SerialName("sr")
    override val timeStretch: Float = 1f,

    @SerialName("parent")
    override val parent: Int? = null,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("masksProperties")
    override val masks: List<Mask>? = null,

    @SerialName("ef")
    override var effects: List<LayerEffect> = emptyList(),

    @SerialName("t")
    private val textData: TextData,

    @SerialName("ao")
    override val autoOrient: BooleanInt = BooleanInt.No,

    @SerialName("tt")
    override val matteMode: MatteMode? = null,

    @SerialName("tp")
    override val matteParent: Int? = null,

    @SerialName("td")
    override val matteTarget: BooleanInt? = null,

    @SerialName("bm")
    override val blendMode: LottieBlendMode = LottieBlendMode.Normal,

    @SerialName("cl")
    override val clazz: String? = null,

    @SerialName("ln")
    override val htmlId: String? = null,

    @SerialName("ct")
    override val collapseTransform: BooleanInt = BooleanInt.No

    ) : BaseLayer() {

    @Transient
    private val fillProperties = DrawProperties(Fill)

    @Transient
    private val fillPaint  =  Paint().apply {
        isAntiAlias = true
    }

    @Transient
    private val strokeProperties = DrawProperties(Stroke(0f))

    @Transient
    private val strokePaint = Paint().apply {
        isAntiAlias = true
    }

    @Transient
    private val textAnimation = textData.ranges.firstOrNull()

    @Transient
    private var textMeasurer: TextMeasurer? = null

    @Transient
    private var lastLayoutDirection: LayoutDirection? = null

    @Transient
    private var lastDensity: Density? = null

    @Transient
    private val textSubLines: MutableList<TextSubLine> = ArrayList()

    @Transient
    private var textStyle: TextStyle = TextStyle.Default

    @Transient
    private val codePointCache = mutableMapOf<Long, String>()

    @Transient
    private val stringBuilder = StringBuilder(2)

    @Transient
    private val matrix = Matrix()

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {
        val document = textData.document.interpolated(state)

        drawScope.drawIntoCanvas { canvas ->
            canvas.save()
            canvas.concat(parentMatrix)

            configurePaint(document, parentAlpha, state)
            configureTextStyle(drawScope, document, state)
            if (state.composition.fontsByFamily.isEmpty() && state.composition.charGlyphs.isNotEmpty()){
                drawTextWithGlyphs(drawScope, document, state)
            } else {
                drawTextWithFonts(drawScope, document)
            }
            canvas.restore()
        }
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect
    ) {
        super.getBounds(drawScope, parentMatrix, applyParents, state, outBounds)

        val composition = state.composition

        outBounds.set(0f, 0f, composition.animation.width, composition.animation.height)
    }

    private fun configurePaint(document: TextDocument, parentAlpha: Float, state: AnimationState) {

        val transformOpacity = transform.opacity?.interpolatedNorm(state) ?: 1f

        val fillOpacity = textAnimation?.style?.fillOpacity?.interpolatedNorm(state) ?: 1f

        val strokeOpacity = textAnimation?.style?.strokeOpacity?.interpolatedNorm(state) ?: 1f

        val fillH = textAnimation?.style?.fillHue?.interpolated(state)?.coerceIn(0f, 360f)
        val fillS = textAnimation?.style?.fillSaturation?.interpolated(state)?.coerceIn(0f, 1f)
        val fillB = textAnimation?.style?.fillBrightness?.interpolated(state)?.coerceIn(0f, 1f)

        fillProperties.color = if (fillH != null && fillS != null && fillB != null) {
            Color.hsl(fillH, fillS, fillB)
        } else {
            textAnimation?.style?.fillColor?.interpolated(state)
                ?: document.fillColor?.toColor() ?: Color.Transparent
        }

        fillProperties.alpha = (parentAlpha * transformOpacity * fillOpacity).coerceIn(0f, 1f)

        fillPaint.color = fillProperties.color
        fillPaint.alpha = fillProperties.alpha

        val strokeH = textAnimation?.style?.strokeHue?.interpolated(state)?.coerceIn(0f, 360f)
        val strokeS = textAnimation?.style?.strokeSaturation?.interpolated(state)?.coerceIn(0f, 1f)
        val strokeB = textAnimation?.style?.strokeBrightness?.interpolated(state)?.coerceIn(0f, 1f)

        strokeProperties.color = if (strokeH != null && strokeS != null && strokeB != null) {
            Color.hsl(strokeH, strokeS, strokeB)
        } else {
            textAnimation?.style?.strokeColor?.interpolated(state)
                ?: document.strokeColor?.toColor() ?: Color.Transparent
        }

        strokeProperties.color = textAnimation?.style?.strokeColor?.interpolated(state)
            ?: document.strokeColor?.toColor() ?: Color.Transparent


        strokeProperties.alpha = (parentAlpha * transformOpacity * strokeOpacity).coerceIn(0f, 1f)

        val strokeWidth = textAnimation?.style?.strokeWidth?.interpolated(state)
            ?: document.strokeWidth

        if (strokeProperties.style.width != strokeWidth) {
            strokeProperties.style = Stroke(width = strokeWidth)
        }

        strokePaint.color = strokeProperties.color
        strokePaint.alpha = strokeProperties.alpha
        strokePaint.strokeWidth = strokeWidth
    }

    private fun configureTextStyle(
        drawScope: DrawScope,
        document: TextDocument,
        animationState: AnimationState
    ) {

        drawScope.run {
            val fontSize = document.fontSize.toSp()
            val baselineShift = document.baselineShift
                ?.let { BaselineShift(it) }
                ?: textStyle.baselineShift

            val fontFamily = animationState.composition.fontsByFamily[document.fontFamily]

            val letterSpacing = textAnimation?.style?.letterSpacing
                ?.interpolated(animationState)?.toSp()
                ?: textStyle.letterSpacing

            val lineSpacing = textAnimation?.style?.lineSpacing
                ?.interpolated(animationState) ?: 0f

            val lineHeight = (document.lineHeight + lineSpacing).toSp()

            if (
                textStyle.fontSize != fontSize ||
                textStyle.baselineShift != baselineShift ||
                textStyle.lineHeight != lineHeight ||
                textStyle.fontFamily != fontFamily ||
                textStyle.letterSpacing != letterSpacing
            ) {
                textStyle = textStyle.copy(
                    baselineShift = baselineShift,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontFamily = fontFamily,
                    letterSpacing = letterSpacing,
                    color = Color.Red
                )
            }
        }
    }

    private fun getTextMeasurer(density: Density, layoutDirection: LayoutDirection): TextMeasurer {
        textMeasurer?.let {
            if (lastDensity == density && lastLayoutDirection == layoutDirection) {
                return it
            }
        }

        val fontFamilyResolver = checkNotNull(
            painterProperties?.fontFamilyResolver
        )

        val tm = TextMeasurer(
            defaultDensity = density,
            defaultLayoutDirection = layoutDirection,
            defaultFontFamilyResolver = fontFamilyResolver
        )

        lastLayoutDirection = layoutDirection
        lastDensity = density
        textMeasurer = tm

        return tm
    }

    private fun drawTextWithFonts(drawScope: DrawScope, document: TextDocument) {
        val measurer = getTextMeasurer(drawScope, drawScope.layoutDirection)

        var tracking = document.textTracking?.div(10f) ?: 0f

        val text = document.text ?: return

        val allLines = getTextLines(text)

        //TODO: tracking animation?
        tracking = drawScope.run {
            tracking.sp.toPx()
        }

        val canvas = drawScope.drawContext.canvas

        allLines.fastForEachIndexed { alLinesIdx, textLine ->
            val boxWidth = document.wrapSize?.firstOrNull() ?: 0f

            val lines = splitGlyphTextIntoLines(measurer, textLine, boxWidth, tracking)

            lines.fastForEachIndexed { idx, line ->

                canvas.save()
                if (offsetCanvas(canvas, drawScope, document, alLinesIdx + idx, line.width)) {
                    drawFontTextLine(
                        line.text,
                        measurer,
                        document,
                        drawScope,
                        canvas,
                        tracking
                    )
                }

                canvas.restore()
            }
        }
    }

    private fun drawTextWithGlyphs(
        drawScope: DrawScope,
        document: TextDocument,
        state: AnimationState
    ) {
        // Split full text in multiple lines
        val textLines = getTextLines(document.text ?: return)
        val tracking = (document.textTracking ?: 0f) / 10f

        val measurer = getTextMeasurer(drawScope, drawScope.layoutDirection)

        val canvas = drawScope.drawContext.canvas

        textLines.fastForEachIndexed { lineIndex, line ->
            val boxWidth = document.wrapSize?.getOrNull(0) ?: 0f

            val lines = splitGlyphTextIntoLines(measurer, line, boxWidth, tracking);

            lines.forEach { l ->
                canvas.save();

                if (offsetCanvas(canvas, drawScope, document, lineIndex, l.width)) {
                    drawGlyphTextLine(
                        text = l.text,
                        state = state,
                        fontScale = document.fontSize,
                        documentData = document,
                        drawScope = drawScope,
                        tracking = tracking
                    )
                }

                canvas.restore();
            }
        }
    }

    private fun getTextLines(text: String): List<String> {
        // Split full text by carriage return character
        val formattedText = text.replace("\r\n".toRegex(), "\r")
            .replace("\u0003".toRegex(), "\r")
            .replace("\n".toRegex(), "\r")
        return formattedText.split("\r".toRegex())
            .dropLastWhile { it.isEmpty() }
    }

    private fun splitGlyphTextIntoLines(
        textMeasurer: TextMeasurer,
        textLine: String,
        boxWidth: Float,
        tracking: Float,
    ): List<TextSubLine> {
        var lineCount = 0

        var currentLineWidth = 0f
        var currentLineStartIndex = 0

        var currentWordStartIndex = 0
        var currentWordWidth = 0f
        var nextCharacterStartsWord = false

        // The measured size of a space.
        var spaceWidth = 0f

        for (i in textLine.indices) {
            val c = textLine[i]
//            if (usingGlyphs) {
//                val characterHash: Int = FontCharacter.hashFor(c, font.getFamily(), font.getStyle())
//                val character: FontCharacter =
//                    composition.getCharacters().get(characterHash) ?: continue
//                currentCharWidth =
//                    character.getWidth() as Float * fontScale * Utils.dpScale() + tracking
//            } else {
//                currentCharWidth = fillPaint.measureText(textLine.substring(i, i + 1)) + tracking
//            }

            val measureResult = textMeasurer.measure(textLine[i].toString(), textStyle)

            val currentCharWidth = measureResult.size.width + tracking

            if (c == ' ') {
                spaceWidth = currentCharWidth
                nextCharacterStartsWord = true
            } else if (nextCharacterStartsWord) {
                nextCharacterStartsWord = false
                currentWordStartIndex = i
                currentWordWidth = currentCharWidth
            } else {
                currentWordWidth += currentCharWidth
            }
            currentLineWidth += currentCharWidth

            if (boxWidth > 0f && currentLineWidth >= boxWidth) {
                if (c == ' ') {
                    // Spaces at the end of a line don't do anything. Ignore it.
                    // The next non-space character will hit the conditions below.
                    continue
                }
                val subLine: TextSubLine =
                    ensureEnoughSubLines(++lineCount)
                if (currentWordStartIndex == currentLineStartIndex) {
                    // Only word on line is wider than box, start wrapping mid-word.
                    val substr = textLine.substring(currentLineStartIndex, i)
                    val trimmed = substr.trim { it <= ' ' }
                    val trimmedSpace = (trimmed.length - substr.length) * spaceWidth
                    subLine.set(trimmed, currentLineWidth - currentCharWidth - trimmedSpace)
                    currentLineStartIndex = i
                    currentLineWidth = currentCharWidth
                    currentWordStartIndex = currentLineStartIndex
                    currentWordWidth = currentCharWidth
                } else {
                    val substr =
                        textLine.substring(currentLineStartIndex, currentWordStartIndex - 1)
                    val trimmed = substr.trim { it <= ' ' }
                    val trimmedSpace = (substr.length - trimmed.length) * spaceWidth
                    subLine.set(
                        trimmed,
                        currentLineWidth - currentWordWidth - trimmedSpace - spaceWidth
                    )
                    currentLineStartIndex = currentWordStartIndex
                    currentLineWidth = currentWordWidth
                }
            }
        }
        if (currentLineWidth > 0f) {
            val line = ensureEnoughSubLines(++lineCount)
            line.set(textLine.substring(currentLineStartIndex), currentLineWidth)
        }
        return textSubLines.subList(0, lineCount)
    }

    private fun ensureEnoughSubLines(numLines: Int): TextSubLine {
        for (i in textSubLines.size until numLines) {
            textSubLines.add(TextSubLine())
        }
        return textSubLines[numLines - 1]
    }

    private fun offsetCanvas(
        canvas: Canvas,
        density: Density,
        document: TextDocument,
        lineIndex: Int,
        lineWidth: Float
    ): Boolean {
        val position = document.wrapPosition?.toOffset()
        val size = document.wrapSize?.let { Size(it[0], it[1]) }
        val lineStartY = if (position == null)
            0f else document.lineHeight + position.y

        val lineOffset: Float = ((lineIndex - 1) * document.lineHeight) + lineStartY

        val clip = painterProperties?.clipTextToBoundingBoxes == true

        if (clip && size != null && position != null &&
            lineOffset >= position.y + size.height + document.fontSize
        ) {
            return false
        }

        val lineStart = position?.x ?: 0f
        val boxWidth = size?.width ?: 0f

        when (document.textJustify) {
            TextJustify.Left -> canvas.translate(lineStart, lineOffset)
            TextJustify.Right -> canvas.translate(lineStart + boxWidth - lineWidth, lineOffset)
            TextJustify.Center -> canvas.translate(
                lineStart + boxWidth / 2f - lineWidth / 2f,
                lineOffset
            )
        }
        return true
    }

    private fun drawFontTextLine(
        text: String,
        textMeasurer: TextMeasurer,
        documentData: TextDocument,
        drawScope: DrawScope,
        canvas: Canvas,
        tracking: Float
    ) {

        var i = 0
        while (i < text.length) {
            val charString: String = codePointToString(text, i)
            i += charString.length


            val measureResult = textMeasurer.measure(charString, textStyle)

            drawCharacterFromFont(measureResult, documentData, drawScope)
            val charWidth = measureResult.size.width
            val tx = charWidth + tracking
            canvas.translate(tx, 0f)
        }
    }

    private fun drawGlyphTextLine(
        text: String,
        state: AnimationState,
        documentData: TextDocument,
        fontScale: Float,
        drawScope: DrawScope,
        tracking: Float
    ) {
        val canvas = drawScope.drawContext.canvas
        text.forEach { c ->
            val character = state.composition.charGlyphs
                .get(documentData.fontFamily)
                ?.get(c.toString()) ?: return@forEach

            drawCharacterAsGlyph(drawScope, state, character, fontScale, documentData)
            val tx = (character.width ?: 0f) * fontScale + tracking
            canvas.translate(tx, 0f)
        }
    }

    private fun drawCharacterAsGlyph(
      drawScope: DrawScope,
      state: AnimationState,
       character : CharacterData,
      fontScale : Float,
      document: TextDocument,
    ) {
        matrix.reset();
        matrix.preTranslate(0f, -(document.baselineShift ?: 0f))
        matrix.preScale(fontScale, fontScale);
        character.data.draw(drawScope, state, matrix, strokePaint, fillPaint)
    }

    private fun codePointToString(text: String, startIndex: Int): String {
        val firstCodePoint: Int = text.codePointAt(startIndex)
        val firstCodePointLength: Int = charCount(firstCodePoint)
        var key = firstCodePoint
        var index = startIndex + firstCodePointLength
        while (index < text.length) {
            val nextCodePoint: Int = text.codePointAt(index)
            if (!isModifier(nextCodePoint)) {
                break
            }
            val nextCodePointLength: Int = charCount(nextCodePoint)
            index += nextCodePointLength
            key = key * 31 + nextCodePoint
        }

        codePointCache[key.toLong()]?.let { return it }

        stringBuilder.setLength(0)
        var i = startIndex
        while (i < index) {
            val codePoint: Int = text.codePointAt(i)
            stringBuilder.addCodePoint(codePoint)
            i += charCount(codePoint)
        }
        val str: String = stringBuilder.toString()
        codePointCache[key.toLong()] = str
        return str
    }

    private fun drawCharacterFromFont(
        character: TextLayoutResult,
        documentData: TextDocument,
        drawScope: DrawScope
    ) {
        if (documentData.strokeOverFill) {
            drawCharacter(character, fillProperties, drawScope)
            drawCharacter(character, strokeProperties, drawScope)
        } else {
            drawCharacter(character, strokeProperties, drawScope)
            drawCharacter(character, fillProperties, drawScope)
        }
    }

    private fun drawCharacter(
        measureResult: TextLayoutResult,
        drawProperties: DrawProperties<*>,
        drawScope: DrawScope
    ) {
        if (drawProperties.color == Color.Transparent || drawProperties.alpha == 0f) {
            return
        }

        if ((drawProperties.style as? Stroke)?.width == 0f) {
            return
        }

        drawScope.drawText(
            textLayoutResult = measureResult,
            color = drawProperties.color,
            alpha = drawProperties.alpha,
        )
    }
}

private class DrawProperties<S : DrawStyle>(
    var style : S,
    var color: Color = Color.Transparent,
    var alpha: Float = 1f
)

private data class TextSubLine(
    var text : String= "",
    var width : Float = 0f
) {
    fun set(text : String, width : Float){
        this.text = text
        this.width = width
    }
}
