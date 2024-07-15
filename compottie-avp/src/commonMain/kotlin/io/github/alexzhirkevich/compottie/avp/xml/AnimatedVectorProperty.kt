package io.github.alexzhirkevich.compottie.avp.xml

import io.github.alexzhirkevich.compottie.avp.DefaultFillAlphaAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultPivotXAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultPivotYAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultRotationAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultScaleXAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultScaleYAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultStrokeAlphaAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultStrokeLineMiterAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultStrokeLineWidthAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultTranslationXAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultTranslationYAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultTrimPathEndAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultTrimPathOffsetAnimator
import io.github.alexzhirkevich.compottie.avp.DefaultTrimPathStartAnimator
import io.github.alexzhirkevich.compottie.avp.EmptyPathAnimator
import io.github.alexzhirkevich.compottie.avp.TransparentFillColorAnimator
import io.github.alexzhirkevich.compottie.avp.TransparentStrokeColorAnimator
import io.github.alexzhirkevich.compottie.avp.animator.FloatAnimator
import io.github.alexzhirkevich.compottie.avp.animator.ObjectAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PaintAnimator
import io.github.alexzhirkevich.compottie.avp.animator.PathAnimator

public sealed class AnimatedVectorProperty<A : ObjectAnimator<*,*>>(
    internal val propertyName : String,
    internal val defaultAnimator : A
) {
    public data object TrimPathStart : AnimatedVectorProperty<FloatAnimator>(
        "trimPathStart",
        DefaultTrimPathStartAnimator
    )

    public data object TrimPathEnd : AnimatedVectorProperty<FloatAnimator>(
        "trimPathEnd",
        DefaultTrimPathEndAnimator
    )

    public data object TrimPathOffset : AnimatedVectorProperty<FloatAnimator>(
        "trimPathOffset",
        DefaultTrimPathOffsetAnimator
    )

    public data object StrokeLineMiter : AnimatedVectorProperty<FloatAnimator>(
        "strokeLineMiter",
        DefaultStrokeLineMiterAnimator
    )

    public data object StrokeLineWidth : AnimatedVectorProperty<FloatAnimator>(
        "strokeLineWidth",
        DefaultStrokeLineWidthAnimator
    )

    public data object StrokeAlpha : AnimatedVectorProperty<FloatAnimator>(
        "strokeAlpha",
        DefaultStrokeAlphaAnimator
    )

    public data object FillAlpha : AnimatedVectorProperty<FloatAnimator>(
        "fillAlpha",
        DefaultFillAlphaAnimator
    )

    public data object Rotation : AnimatedVectorProperty<FloatAnimator>(
        "rotation", DefaultRotationAnimator)
    public data object ScaleX : AnimatedVectorProperty<FloatAnimator>(
        "scaleX",
        DefaultScaleXAnimator
    )

    public data object ScaleY : AnimatedVectorProperty<FloatAnimator>(
        "scaleY",
        DefaultScaleYAnimator
    )

    public data object TranslationX : AnimatedVectorProperty<FloatAnimator>(
        "translationX",
        DefaultTranslationXAnimator)

    public data object TranslationY : AnimatedVectorProperty<FloatAnimator>(
        "translationY",
        DefaultTranslationYAnimator
    )

    public data object PivotX : AnimatedVectorProperty<FloatAnimator>(
        "pivotX",
        DefaultPivotXAnimator
    )

    public data object PivotY : AnimatedVectorProperty<FloatAnimator>(
        "pivotY",
        DefaultPivotYAnimator
    )

    public data object PathData : AnimatedVectorProperty<PathAnimator>(
        "pathData",
        EmptyPathAnimator
    )

    public data object FillColor : AnimatedVectorProperty<PaintAnimator>(
        "fillColor",
        TransparentFillColorAnimator
    )

    public data object StrokeColor : AnimatedVectorProperty<PaintAnimator>(
        "color",
        TransparentStrokeColorAnimator
    )


    public companion object {
        public fun forName(name: String): AnimatedVectorProperty<*> = when (name) {
            TrimPathStart.propertyName -> TrimPathStart
            TrimPathEnd.propertyName -> TrimPathEnd
            TrimPathOffset.propertyName -> TrimPathOffset
            StrokeLineMiter.propertyName -> StrokeLineMiter
            StrokeLineWidth.propertyName -> StrokeLineWidth
            StrokeAlpha.propertyName -> StrokeAlpha
            FillAlpha.propertyName -> FillAlpha
            Rotation.propertyName -> Rotation
            ScaleX.propertyName -> ScaleX
            ScaleY.propertyName -> ScaleY
            TranslationX.propertyName -> TranslationX
            TranslationY.propertyName -> TranslationY
            PivotX.propertyName -> PivotX
            PivotY.propertyName -> PivotY
            PathData.propertyName -> PathData
            else -> error("Unknown VectorProperty: $name")
        }
    }
}
