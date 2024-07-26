package expressions

import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.Animation
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTextDocument
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.layers.NullLayer
import io.github.alexzhirkevich.compottie.mockFontFamilyResolver
import kotlin.test.assertEquals

internal const val ret = "\$bm_rt"

internal fun String.assertSimpleExprEquals(expected : Any) {
    "$ret=$this".assertExprReturns(expected)
}

internal fun String.assertSimpleExprReturns(expected : Any) {
    val value = AnimatedNumber.Default(0f, this)
    val state = MockAnimationState(0f)
    val evaluator = ExpressionEvaluator(this)
    assertEquals(expected, value.run { evaluator.run { evaluate(state) } })
}

internal fun String.assertExprReturns(expected : Any) {
    val value = AnimatedNumber.Default(0f, this)
    val state = MockAnimationState(0f)
    val evaluator = ExpressionEvaluator(this)

    assertEquals(expected, value.run { evaluator.run { evaluate(state) } })
}

internal fun String.assertExprEquals(expected : Any) {
    "$ret = $this".assertExprReturns(expected)
}


internal fun String.assertExprReturns(expected : Float) {
    val value = AnimatedNumber.Default(0f, this)
    val state = MockAnimationState(0f)
    assertEquals(expected, value.interpolated(state), absoluteTolerance = 0.00001f)
}

internal fun String.assertExprEquals(expected : Float) {
    "$ret = $this".assertExprReturns(expected)
}

internal fun String.assertExprReturns(expected : String) {
    val value = AnimatedTextDocument(expression = this, keyframes = emptyList())
    val state = MockAnimationState(0f)
    assertEquals(expected, value.interpolated(state).text)
}

internal fun String.assertExprReturns(expected : Vec2) {
    val value = AnimatedVector2.Default(listOf(0f,0f), this)
    val state = MockAnimationState(0f)
    assertEquals(expected, value.interpolated(state))
}
internal fun String.assertExprEquals(expected : Vec2) {
    "$ret = $this".assertExprReturns(expected)
}
internal fun String.assertExprEquals(expected : String) {
    "$ret = $this".assertExprReturns(expected)
}

internal fun MockAnimationState(
    frame : Float,
    durationFrames : Float = 120f
) = AnimationState(
    composition = LottieComposition(
        Animation(
            frameRate = 24f,
            width = 1024f,
            height = 1024f,
            version = "5.0.0",
            inPoint = 0f,
            outPoint = durationFrames,
            name = "Animation"
        )
    ),
    assets = emptyMap(),
    applyOpacityToLayers = false,
    enableExpressions = true,
    fonts = emptyMap(),
    frame = frame,
    fontFamilyResolver = mockFontFamilyResolver(),
    clipToCompositionBounds = true,
    clipTextToBoundingBoxes = true,
    enableMergePaths = true,
    enableTextGrouping = false,
    layer = NullLayer()
)