package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicTextLayerProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.compottie.internal.animation.expressions.toJs
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.animation.toColor
import io.github.alexzhirkevich.compottie.internal.assets.CharacterData
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BidiClass
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanIntSerializer
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.helpers.bidiClassOf
import io.github.alexzhirkevich.compottie.internal.helpers.isJoiningRightToLeft
import io.github.alexzhirkevich.compottie.internal.helpers.requiresBidi
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextData
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextDocument
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextJustify
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextRangeUnits
import io.github.alexzhirkevich.compottie.internal.helpers.text.fontScale
import io.github.alexzhirkevich.compottie.internal.platform.addCodePoint
import io.github.alexzhirkevich.compottie.internal.platform.charCount
import io.github.alexzhirkevich.compottie.internal.platform.codePointAt
import io.github.alexzhirkevich.compottie.internal.platform.isModifier
import io.github.alexzhirkevich.compottie.internal.utils.fastReset
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import io.github.alexzhirkevich.compottie.internal.utils.preTranslate
import io.github.alexzhirkevich.compottie.internal.utils.toOffset
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.JsPropertyAccessor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("5")
internal class TextLayer(
    @SerialName("ks")
    override val transform: Transform = Transform(),

    @SerialName("ind")
    override val index: Int? = null,

    @SerialName("ip")
    override val inPoint: Float? = null,

    @SerialName("op")
    override val outPoint: Float? = null,

    @SerialName("st")
    override val startTime: Float? = null,

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

    @SerialName("hasMask")
    override val hasMask: Boolean? = null,

    @SerialName("ef")
    override var effects: List<LayerEffect> = emptyList(),

    @SerialName("ao")
    @Serializable(with = BooleanIntSerializer::class)
    override val autoOrient: Boolean = false,

    @SerialName("tt")
    override val matteMode: MatteMode? = null,

    @SerialName("tp")
    override val matteParent: Int? = null,

    @SerialName("td") @Serializable(with = BooleanIntSerializer::class)
    override val matteTarget: Boolean? = null,

    @SerialName("bm")
    override val blendMode: LottieBlendMode = LottieBlendMode.Normal,

    @SerialName("t")
    private val textData: TextData,

    ) : BaseLayer() {

    @Transient
    private val fillProperties = DrawProperties(Fill)

    @Transient
    private val fillPaint = Paint().apply {
        isAntiAlias = true
    }

    @Transient
    private val strokeProperties = DrawProperties(Stroke(0f))

    @Transient
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = PaintingStyle.Stroke
    }

    @Transient
    private val textAnimation = textData.ranges.firstOrNull()

    /**
     * If this is paragraph text, one line may wrap depending on the size of the document data box.
     */
    @Transient
    private val textSubLines: MutableList<TextSubLine> = ArrayList()

    @Transient
    private var textStyle: TextStyle = TextStyle.Default

    @Transient
    private val codePointCache = mutableMapOf<Long, String>()

    // Capacity is 2 because emojis are 2 characters. Some are longer in which case, the capacity will
    // be expanded but that should be pretty rare.
    @Transient
    private val stringBuilder = StringBuilder(2)

    @Transient
    private val charStringBuilder = StringBuilder(0)

    @Transient
    private val reorderingStringBuilder = StringBuilder(0)

    @Transient
    private val charStrings = ArrayList<String>()

    // Pieces of the current line in drawing order, prepared by [layoutFontTextLine]
    @Transient
    private val lineGroupIndices = ArrayList<Int>()

    @Transient
    private val lineGroupLayouts = ArrayList<TextLayoutResult>()

    @Transient
    private val matrix = Matrix()

    override fun deepCopy(): Layer {
        return TextLayer(
            transform = transform.deepCopy(),
            index = index,
            inPoint = inPoint,
            outPoint = outPoint,
            startTime = startTime,
            name = name,
            timeStretch = timeStretch,
            parent = parent,
            hidden = hidden,
            masks = masks?.map(Mask::deepCopy),
            hasMask = hasMask,
            effects = effects.map(LayerEffect::copy),
            textData = textData.deepCopy(),
            autoOrient = autoOrient,
            matteMode = matteMode,
            matteParent = matteParent,
            matteTarget = matteTarget,
            blendMode = blendMode,
        )
    }

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

            configurePaint(document, parentAlpha, state, 0)

            val hasFontFamily = configureTextStyle(drawScope, document, state)

            val ascent = document.fontFamily?.let {
                state.composition.animation.fonts?.find(it)
            }?.ascent ?: 0f

            if (hasFontFamily) {
                drawTextWithFonts(
                    state = state,
                    ascent = ascent,
                    drawScope = drawScope,
                    document = document,
                    parentAlpha = parentAlpha,
                )
            } else {
                val glyphs = state.composition.findGlyphs(document.fontFamily)

                if (glyphs != null) {
                    drawTextWithGlyphs(
                        drawScope = drawScope,
                        document = document,
                        ascent = ascent,
                        state = state,
                        glyphs = glyphs,
                        parentAlpha = parentAlpha,
                    )
                } else {
                    drawTextWithFonts(
                        state = state,
                        ascent = ascent,
                        drawScope = drawScope,
                        document = document,
                        parentAlpha = parentAlpha,
                    )
                }
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

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property?.toString()) {
            "text" -> jsCache.getOrPut("text") {
                runtime.makeObject(
                    mapOf(
                        "sourceText".toJs(runtime) to JsPropertyAccessor
                            .BackedField(getter = Callable {
                                textData.document.raw(state).text?.toJs(runtime)
                            })
                    )
                )
            }

            else -> super.get(property, runtime)
        }
    }

    override fun setDynamicProperties(
        composition: DynamicCompositionProvider?,
        state: AnimationState
    ): DynamicLayerProvider? {
        return super.setDynamicProperties(composition, state).also {
            textData.document.dynamic = it as? DynamicTextLayerProvider
        }
    }

    /**
     * Configures [fillPaint]/[strokePaint] (and their [DrawProperties]) based on the currently
     * active text range.
     *
     * The text range style is applied only to characters that are inside the range selection,
     * so this is called for every character, with its [indexInDocument].
     */
    private fun configurePaint(
        document: TextDocument,
        parentAlpha: Float,
        state: AnimationState,
        indexInDocument: Int
    ) {
        // Range style is applied only if the character is inside the range selection.
        // Otherwise, fall back to the document properties.
        val style = textAnimation?.style?.takeIf {
            isIndexInRangeSelection(indexInDocument, document, state)
        }

        val transformOpacity = transform.opacity.interpolatedNorm(state)
        val fillOpacity = style?.fillOpacity?.interpolatedNorm(state) ?: 1f
        val strokeOpacity = style?.strokeOpacity?.interpolatedNorm(state) ?: 1f

        val fillH = style?.fillHue?.interpolatedFloat(state) ?: -1f
        val fillS = style?.fillSaturation?.interpolatedFloat(state) ?: -1f
        val fillB = style?.fillBrightness?.interpolatedFloat(state) ?: -1f

        fillProperties.color = if (fillH >= 0 && fillS >= 0f && fillB >= 0f) {
            Color.hsl(
                fillH.coerceIn(0f, 360f),
                fillS.coerceIn(0f, 1f),
                fillB.coerceIn(0f, 1f)
            )
        } else {
            style?.fillColor?.interpolatedColor(state)
                ?.let(::Color)
                ?: document.fillColor?.toColor() ?: Color.Transparent
        }

        fillProperties.alpha = (parentAlpha * transformOpacity * fillOpacity).coerceIn(0f, 1f)

        fillPaint.color = fillProperties.color
        fillPaint.alpha = fillProperties.alpha * fillProperties.color.alpha

        val strokeH = style?.strokeHue?.interpolatedFloat(state) ?: -1f
        val strokeS = style?.strokeSaturation?.interpolatedFloat(state) ?: -1f
        val strokeB = style?.strokeBrightness?.interpolatedFloat(state) ?: -1f

        strokeProperties.color = if (strokeH >= 0 && strokeS >= 0 && strokeB >= 0) {
            Color.hsl(strokeH.coerceIn(0f, 360f), strokeS.coerceIn(0f, 1f), strokeB.coerceIn(0f, 1f))
        } else {
            style?.strokeColor?.interpolatedColor(state)
                ?.let(::Color)
                ?: document.strokeColor?.toColor() ?: Color.Transparent
        }

        strokeProperties.alpha = (parentAlpha * transformOpacity * strokeOpacity).coerceIn(0f, 1f)

        val strokeWidth = style?.strokeWidth?.interpolatedFloat(state)
            ?: document.strokeWidth

        if (strokeProperties.style.width != strokeWidth) {
            strokeProperties.style = Stroke(width = strokeWidth)
        }

        strokePaint.color = strokeProperties.color
        strokePaint.alpha = strokeProperties.alpha * strokeProperties.color.alpha
        strokePaint.strokeWidth = strokeWidth
    }

    /**
     * Whether the character at [indexInDocument] is inside the text range selector.
     * Without a range selector (or without start/end) every character is selected.
     *
     * After Effects supports reversed ranges where the start is greater than the end.
     * For the purposes of this check the smaller value is always used as the start.
     */
    private fun isIndexInRangeSelection(
        indexInDocument: Int,
        document: TextDocument,
        state: AnimationState
    ): Boolean {
        val selector = textAnimation?.selector ?: return true

        val startValue = selector.start?.interpolatedFloat(state) ?: return true
        val endValue = selector.end?.interpolatedFloat(state) ?: return true

        var rangeStart = minOf(startValue, endValue)
        var rangeEnd = maxOf(startValue, endValue)

        selector.offset?.interpolatedFloat(state)?.let { offset ->
            rangeStart += offset
            rangeEnd += offset
        }

        // Index is the default, same as in lottie-android
        return if (selector.rangeUnits == null || selector.rangeUnits == TextRangeUnits.Index) {
            indexInDocument >= rangeStart && indexInDocument < rangeEnd
        } else {
            val textLength = document.text?.length ?: 0
            if (textLength == 0) {
                return true
            }
            val currentIndexAsPercent = indexInDocument / textLength.toFloat() * 100f
            currentIndexAsPercent >= rangeStart && currentIndexAsPercent < rangeEnd
        }
    }

    private fun configureTextStyle(
        drawScope: DrawScope,
        document: TextDocument,
        animationState: AnimationState
    ): Boolean = drawScope.run {

        val fontSize = document.fontSize.toSp()

        val baselineShift = document.baselineShift
            ?.let { BaselineShift(it) }
            ?: textStyle.baselineShift

        val strFontFamily = document.fontFamily ?: return@run false

        val fontFamily = animationState.fonts[strFontFamily]

        val fontSpec = animationState.composition.animation.fonts?.find(strFontFamily)

        val weight = fontSpec?.weight ?: FontWeight.Normal
        val style = fontSpec?.style ?: FontStyle.Normal

        val letterSpacing = textAnimation?.style?.letterSpacing
            ?.interpolatedFloat(animationState)?.toSp()
            ?: textStyle.letterSpacing

        val lineHeight = document.lineHeight.toSp()

        if (
            textStyle.fontSize != fontSize ||
            textStyle.baselineShift != baselineShift ||
            textStyle.lineHeight != lineHeight ||
            textStyle.fontFamily != fontFamily ||
            textStyle.letterSpacing != letterSpacing ||
            textStyle.fontWeight != weight ||
            textStyle.fontStyle != style
        ) {
            textStyle = textStyle.copy(
                baselineShift = baselineShift,
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                fontWeight = weight,
                fontStyle = style
            )
        }

        fontFamily != null
    }

    private fun drawTextWithFonts(
        state: AnimationState,
        ascent: Float,
        drawScope: DrawScope,
        document: TextDocument,
        parentAlpha: Float,
    ) {
        var tracking = document.textTracking?.div(10f) ?: 0f

        val text = document.text ?: return

        val allLines = getTextLines(text)

        // Tracking animation is applied through the text style letter spacing (see configureTextStyle)
        tracking = drawScope.run {
            tracking.sp.toPx()
        }

        val canvas = drawScope.drawContext.canvas

        // Running line index across all (wrapped) lines of the document
        var lineIndex = -1
        var characterIndexAtStartOfLine = 0

        allLines.fastForEachIndexed { _, textLine ->

            val boxWidth = document.wrapSize?.firstOrNull() ?: 0f

            val lines = splitGlyphTextIntoLines(
                textMeasurer = state.textMeasurer,
                density = drawScope,
                layoutDirection = drawScope.layoutDirection,
                textLine = textLine,
                fontScale = document.fontScale,
                boxWidth = boxWidth,
                tracking = tracking,
                glyphs = null
            )

            lines.fastForEachIndexed { _, line ->
                lineIndex++

                canvas.save()

                val lineWidth =  layoutFontTextLine(
                    text = line.text,
                    textMeasurer = state.textMeasurer,
                    tracking = tracking
                )
                if (offsetCanvas(
                        state = state,
                        canvas = canvas,
                        document = document,
                        lineIndex = lineIndex,
                        lineWidth = lineWidth,
                        accent = ascent,
                        withFonts = true
                    )
                ) {
                    drawFontTextLine(
                        text = line.text,
                        state = state,
                        textMeasurer = state.textMeasurer,
                        documentData = document,
                        drawScope = drawScope,
                        canvas = canvas,
                        tracking = tracking,
                        characterIndexAtStartOfLine = characterIndexAtStartOfLine,
                        parentAlpha = parentAlpha,
                    )
                }

                characterIndexAtStartOfLine += line.text.length

                canvas.restore()
            }
        }
    }

    private fun drawTextWithGlyphs(
        drawScope: DrawScope,
        document: TextDocument,
        ascent: Float,
        state: AnimationState,
        glyphs: Map<String, CharacterData>,
        parentAlpha: Float,
    ) {
        // Split full text in multiple lines
        val textLines = getTextLines(document.text ?: return)

        // Add tracking (including the animated one from the text range style)
        val tracking = (document.textTracking ?: 0f) / 10f +
                (textAnimation?.style?.letterSpacing?.interpolatedFloat(state) ?: 0f)

        val canvas = drawScope.drawContext.canvas

        // Running line index across all (wrapped) lines of the document
        var lineIndex = -1
        var characterIndexAtStartOfLine = 0

        textLines.fastForEachIndexed { _, line ->

            val boxWidth = document.wrapSize?.getOrNull(0) ?: 0f

            val lines = splitGlyphTextIntoLines(
                textMeasurer = state.textMeasurer,
                density = drawScope,
                layoutDirection = drawScope.layoutDirection,
                textLine = line,
                fontScale = document.fontScale,
                boxWidth = boxWidth,
                tracking = tracking,
                glyphs = glyphs
            )

            lines.forEach { l ->
                lineIndex++

                canvas.save()

                if (offsetCanvas(
                        state = state,
                        canvas = canvas,
                        document = document,
                        lineIndex = lineIndex,
                        lineWidth = l.width,
                        accent = ascent,
                        withFonts = false
                    )
                ) {
                    drawGlyphTextLine(
                        text = l.text,
                        state = state,
                        fontScale = document.fontScale,
                        documentData = document,
                        drawScope = drawScope,
                        tracking = tracking,
                        glyphs = glyphs,
                        characterIndexAtStartOfLine = characterIndexAtStartOfLine,
                        parentAlpha = parentAlpha,
                    )
                }

                characterIndexAtStartOfLine += l.text.length

                canvas.restore()
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
        density: Density,
        layoutDirection: LayoutDirection,
        textLine: String,
        fontScale: Float,
        boxWidth: Float,
        tracking: Float,
        glyphs: Map<String, CharacterData>?,
    ): List<TextSubLine> {
        var lineCount = 0
        var currentLineWidth = 0f
        var currentLineStartIndex = 0
        var currentWordStartIndex = 0
        var currentWordWidth = 0f
        var nextCharacterStartsWord = false

        // The measured size of a space.
        var spaceWidth = 0f

        // Font text is measured exactly like it is drawn: by pieces (a character with its marks,
        // or a whole group of joined RTL characters) instead of by isolated UTF-16 chars.
        // Isolated arabic letters (and standalone diacritics) are wider than in a joined word.
        val pieceWidths = if (glyphs == null) {
            measurePieceWidths(textMeasurer, density, layoutDirection, textLine, tracking)
        } else null

        for (i in textLine.indices) {
            val c = textLine[i]

            val currentCharWidth = if (glyphs != null) {
                // Something is wrong. Potentially, they didn't export the text as a glyph.
                // Such characters are not drawn, so they must not take any space either.
                val character = glyphs[c.toString()] ?: continue

                (character.width ?: 0f) * fontScale + tracking
            } else {
                pieceWidths!![i]
            }

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

    /**
     * Width (including tracking) of every piece of the line that is drawn at once, stored at the
     * index where the piece starts. The rest of the piece's chars have zero width.
     * Pieces are the same as in [layoutFontTextLine].
     */
    private fun measurePieceWidths(
        textMeasurer: TextMeasurer,
        density: Density,
        layoutDirection: LayoutDirection,
        textLine: String,
        tracking: Float,
    ): FloatArray {
        val widths = FloatArray(textLine.length)

        var i = 0
        while (i < textLine.length) {
            val cluster = codePointToString(textLine, i)
            var end = i + cluster.length

            if (isJoiningRightToLeft(cluster)) {
                while (end < textLine.length) {
                    val next = codePointToString(textLine, end)
                    if (!isJoiningRightToLeft(next)) {
                        break
                    }
                    end += next.length
                }
            }

            widths[i] = textMeasurer.measure(
                text = textLine.substring(i, end),
                style = textStyle,
                density = density,
                layoutDirection = layoutDirection,
            ).size.width + tracking

            i = end
        }

        return widths
    }

    /**
     * Elements are reused and not deleted to save allocations.
     */
    private fun ensureEnoughSubLines(numLines: Int): TextSubLine {
        for (i in textSubLines.size until numLines) {
            textSubLines.add(TextSubLine())
        }
        return textSubLines[numLines - 1]
    }

    private fun offsetCanvas(
        state: AnimationState,
        canvas: Canvas,
        document: TextDocument,
        lineIndex: Int,
        lineWidth: Float,
        accent: Float,
        withFonts: Boolean,
    ): Boolean {
        val position = document.wrapPosition?.toOffset()
            ?.plus(Offset(x = 0f, y = accent / 100f * document.fontSize))
            ?: Offset.Zero

        val size = document.wrapSize?.let { Size(it[0], it[1]) } ?: Size.Zero

        val lineSpacing = textAnimation?.style?.lineSpacing?.interpolatedFloat(state) ?: 0f

        var lineOffsetY = (lineIndex * (document.lineHeight + lineSpacing)) + position.y

        if (withFonts) {
            // compose draws text below the Y. Lottie expects it to be above the Y
            lineOffsetY -= document.fontSize
        }

        // Clip only text that actually has a bounding box
        if (state.clipTextToBoundingBoxes &&
            document.wrapSize != null &&
            document.wrapPosition != null &&
            lineOffsetY >= position.y + size.height + document.fontSize
        ) {
            return false
        }

        when (document.textJustify) {
            TextJustify.Left, TextJustify.LastLineLeft ->
                canvas.translate(position.x, lineOffsetY)

            TextJustify.Right, TextJustify.LastLineRight ->
                canvas.translate(position.x + size.width - lineWidth, lineOffsetY)

            TextJustify.Center, TextJustify.LastLineCenter ->
                canvas.translate(position.x + (size.width - lineWidth) / 2f, lineOffsetY)
        }
        return true
    }

    /**
     * @param characterIndexAtStartOfLine The index within the overall document of the character at the start of the line
     */
    private fun drawFontTextLine(
        text: String,
        state: AnimationState,
        textMeasurer: TextMeasurer,
        documentData: TextDocument,
        drawScope: DrawScope,
        canvas: Canvas,
        tracking: Float,
        characterIndexAtStartOfLine: Int,
        parentAlpha: Float,
    ) {
        // Pieces were prepared (and measured) by layoutFontTextLine
        for (idx in lineGroupLayouts.indices) {
            configurePaint(
                documentData,
                parentAlpha,
                state,
                characterIndexAtStartOfLine + lineGroupIndices[idx]
            )

            val measureResult = lineGroupLayouts[idx]

            drawCharacterFromFont(measureResult, documentData, drawScope)

            canvas.translate(measureResult.size.width + tracking, 0f)
        }
    }

    /**
     * Splits the line into the pieces that are drawn one by one from left to right and measures
     * them. RTL text is transformed to the visual order first, and joining RTL characters are
     * grouped, so the text measurer can pick the correct joining glyphs.
     *
     * The result is stored in [lineGroupIndices] and [lineGroupLayouts].
     *
     * @return the width that will really be drawn, so the line can be aligned precisely
     */
    private fun layoutFontTextLine(
        text: String,
        textMeasurer: TextMeasurer,
        tracking: Float,
    ): Float {
        lineGroupIndices.clear()
        lineGroupLayouts.clear()

        val currentLineText = if (requiresBidi(text)) reorderLineVisually(text) else text

        charStrings.clear()

        var i = 0
        while (i < currentLineText.length) {
            val charString = codePointToString(currentLineText, i)
            charStrings.add(charString)
            i += charString.length
        }

        var width = 0f

        i = 0
        while (i < charStrings.size) {
            charStringBuilder.setLength(0)
            charStringBuilder.append(charStrings[i])

            // Only a joining RTL character can start a group. Otherwise a space (or a latin letter,
            // digit, punctuation) would swallow the arabic word that follows it, producing strings
            // like "word " whose trailing space is measured/placed differently by the text measurer.
            val startsJoiningGroup = isJoiningRightToLeft(charStrings[i])

            var nextIndex = i + 1
            while (startsJoiningGroup && nextIndex < charStrings.size) {
                val nextCharString = charStrings[nextIndex]
                if (isJoiningRightToLeft(nextCharString)) {
                    // We insert at the front explicitly, string builder will actually add it
                    // to the end visually since it is rtl
                    charStringBuilder.insert(0, nextCharString)
                    nextIndex++
                } else {
                    break
                }
            }

            val layout = textMeasurer.measure(charStringBuilder.toString(), textStyle)

            lineGroupIndices.add(i)
            lineGroupLayouts.add(layout)

            width += layout.size.width
            // Tracking is not applied after the last piece
            if (nextIndex < charStrings.size) {
                width += tracking
            }

            i = nextIndex
        }

        return width
    }

    /**
     * Transform string stored in logical order to visual order.
     * For example, if an RTL language is stored as qwert in memory, it will become trewq,
     * so we can keep the logic of drawing each character from left to right afterward.
     *
     * The paragraph direction is taken from the first strong character of the line
     * (same as Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT: LTR only if there is no strong character),
     * so a line that starts with Arabic/Hebrew is an RTL paragraph and its embedded LTR words
     * ("One", "Two") end up in reversed order relative to the LTR case.
     *
     * See [bidiClassOf] for the limitations of this approximation of the Unicode bidi algorithm.
     *
     * @param text the original string
     * @return the text in visual order, written from left to right
     */
    private fun reorderLineVisually(text: String): String {
        val clusters = ArrayList<String>()
        var i = 0
        while (i < text.length) {
            val cluster = codePointToString(text, i)
            clusters.add(cluster)
            i += cluster.length
        }

        val n = clusters.size
        val classes = Array(n) { bidiClassOf(clusters[it].codePointAt(0)) }

        // Paragraph direction: the first strong character wins
        var baseRtl = false
        for (c in classes) {
            if (c == BidiClass.RTL) {
                baseRtl = true
                break
            }
            if (c == BidiClass.LTR) {
                break
            }
        }

        // Nearest preceding strong (L/R) character direction. Used to resolve digits.
        val prevStrongIsRtl = BooleanArray(n)
        var lastStrongIsRtl = baseRtl
        for (idx in 0 until n) {
            prevStrongIsRtl[idx] = lastStrongIsRtl
            when (classes[idx]) {
                BidiClass.RTL -> lastStrongIsRtl = true
                BidiClass.LTR -> lastStrongIsRtl = false
                else -> Unit
            }
        }

        // Direction of non-neutral characters (digits act as RTL in RTL context, as LTR after LTR text)
        // and the resolved levels of everything that is not a neutral.
        val levels = IntArray(n)
        val dirIsRtl = BooleanArray(n)
        for (idx in 0 until n) {
            when (classes[idx]) {
                BidiClass.RTL -> {
                    dirIsRtl[idx] = true
                    levels[idx] = 1
                }
                BidiClass.LTR -> {
                    dirIsRtl[idx] = false
                    levels[idx] = if (baseRtl) 2 else 0
                }
                BidiClass.DIGIT -> {
                    val rtlContext = baseRtl || prevStrongIsRtl[idx]
                    dirIsRtl[idx] = rtlContext
                    levels[idx] = if (rtlContext) 2 else 0
                }
                BidiClass.NEUTRAL -> Unit
            }
        }

        // Direction of the nearest non-neutral character after each position (end of line = paragraph direction)
        val nextIsRtl = BooleanArray(n)
        var next = baseRtl
        for (idx in n - 1 downTo 0) {
            nextIsRtl[idx] = next
            if (classes[idx] != BidiClass.NEUTRAL) {
                next = dirIsRtl[idx]
            }
        }

        // Neutrals take the direction of the surrounding text if both sides agree,
        // otherwise the paragraph direction. Start of line = paragraph direction.
        var prev = baseRtl
        for (idx in 0 until n) {
            if (classes[idx] == BidiClass.NEUTRAL) {
                val rtl = if (prev == nextIsRtl[idx]) prev else baseRtl
                levels[idx] = if (rtl) 1 else if (baseRtl) 2 else 0
            } else {
                prev = dirIsRtl[idx]
            }
        }

        var maxLevel = 0
        var minOddLevel = Int.MAX_VALUE
        for (level in levels) {
            if (level > maxLevel) maxLevel = level
            if (level % 2 == 1 && level < minOddLevel) minOddLevel = level
        }

        val order = IntArray(n) { it }

        // Reverse every sequence of characters at level >= L, from the highest level down to
        // the lowest odd level
        if (minOddLevel != Int.MAX_VALUE) {
            for (level in maxLevel downTo minOddLevel) {
                var start = 0
                while (start < n) {
                    if (levels[order[start]] >= level) {
                        var end = start
                        while (end < n && levels[order[end]] >= level) {
                            end++
                        }
                        var lo = start
                        var hi = end - 1
                        while (lo < hi) {
                            val tmp = order[lo]
                            order[lo] = order[hi]
                            order[hi] = tmp
                            lo++
                            hi--
                        }
                        start = end
                    } else {
                        start++
                    }
                }
            }
        }

        reorderingStringBuilder.setLength(0)
        for (idx in 0 until n) {
            reorderingStringBuilder.append(clusters[order[idx]])
        }

        return reorderingStringBuilder.toString()
    }

    /**
     * @param characterIndexAtStartOfLine The index within the overall document of the character at the start of the line
     */
    private fun drawGlyphTextLine(
        text: String,
        state: AnimationState,
        documentData: TextDocument,
        fontScale: Float,
        drawScope: DrawScope,
        tracking: Float,
        glyphs: Map<String, CharacterData>,
        characterIndexAtStartOfLine: Int,
        parentAlpha: Float,
    ) {
        val canvas = drawScope.drawContext.canvas

        text.forEachIndexed { index, c ->

            val character = glyphs[c.toString()] ?: return@forEachIndexed

            configurePaint(documentData, parentAlpha, state, characterIndexAtStartOfLine + index)

            drawCharacterAsGlyph(drawScope, state, character, fontScale, documentData)

            val tx = (character.width ?: 0f) * fontScale + tracking
            canvas.translate(tx, 0f)
        }
    }

    private fun drawCharacterAsGlyph(
        drawScope: DrawScope,
        state: AnimationState,
        character: CharacterData,
        fontScale: Float,
        document: TextDocument,
    ) {
        matrix.fastReset()
        matrix.preTranslate(0f, -(document.baselineShift ?: 0f))
        matrix.preScale(fontScale, fontScale)

        character.data?.draw(drawScope, state, matrix, strokePaint, fillPaint)
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
    var style: S,
    var color: Color = Color.Transparent,
    var alpha: Float = 1f
)

private data class TextSubLine(
    var text: String = "",
    var width: Float = 0f
) {
    fun set(text: String, width: Float) {
        this.text = text
        this.width = width
    }
}

