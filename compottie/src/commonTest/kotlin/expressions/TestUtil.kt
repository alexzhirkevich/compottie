package expressions

import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.Animation
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.layers.NullLayer
import io.github.alexzhirkevich.compottie.mockFontFamilyResolver
import kotlin.test.assertEquals

internal const val ret = "\$bm_rt"


internal fun String.assertExprReturns(expected : Float) {
    val value = AnimatedNumber.Default(0f, this)
    val state = MockAnimationState(0f)
    assertEquals(expected, value.interpolated(state), absoluteTolerance = 0.00001f)
}

internal fun String.assertExprValueEquals(expected : Float) {
    "$ret = $this".assertExprReturns(expected)
}

internal fun String.assertExprReturns(expected : Vec2) {
    val value = AnimatedVector2.Default(listOf(0f,0f), this)
    val state = MockAnimationState(0f)
    assertEquals(expected, value.interpolated(state))
}
internal fun String.assertExprValueEquals(expected : Vec2) {
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
    layer = NullLayer()
)