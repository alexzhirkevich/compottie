package io.github.alexzhirkevich.compottie.avp.xml

internal enum class VectorProperty(val property : String) {
    TrimPathStart("trimPathStart"),
    TrimPathEnd("trimPathEnd"),
    TrimPathOffset("trimPathOffset"),
    StrokeLineMiter("strokeLineMiter"),
    StrokeAlpha("strokeAlpha"),
    FillAlpha("fillAlpha"),
    Rotation("rotation"),
    ScaleX("scaleX"),
    ScaleY("scaleY"),
    TranslationX("translationX"),
    TranslationY("translationY"),
    PivotX("pivotX"),
    PivotY("pivotY"),
}