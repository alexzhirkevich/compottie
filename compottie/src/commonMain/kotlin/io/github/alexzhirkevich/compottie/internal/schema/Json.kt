package io.github.alexzhirkevich.compottie.internal.schema

import io.github.alexzhirkevich.compottie.internal.schema.layers.Layer
import io.github.alexzhirkevich.compottie.internal.schema.layers.NullLayer
import io.github.alexzhirkevich.compottie.internal.schema.layers.ShapeLayer
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.shapes.EllipseShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.FillShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientFill
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientStroke
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GroupShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.RectShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Shape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.SolidStrokeShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.TransformShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.TrimPathShape
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalSerializationApi::class)
val LottieJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    allowTrailingComma = true
    serializersModule = SerializersModule {
        polymorphic(Layer::class){
            subclass(ShapeLayer::class)
            subclass(NullLayer::class)
        }

        polymorphic(AnimatedValue::class){
            subclass(AnimatedValue.Default::class)
            subclass(AnimatedValue.Keyframed::class)
        }
        polymorphic(AnimatedVector2::class){
            subclass(AnimatedVector2.Keyframed::class)
            subclass(AnimatedVector2.Default::class)
        }
        polymorphic(Shape::class){
//            subclass(Path::class)
            subclass(EllipseShape::class)
            subclass(FillShape::class)
            subclass(GradientFill::class)
            subclass(GroupShape::class)
            subclass(RectShape::class)
//            subclass(Round::class)
            subclass(SolidStrokeShape::class)
            subclass(GradientStroke::class)
            subclass(TrimPathShape::class)
            subclass(TransformShape::class)
        }
    }
}