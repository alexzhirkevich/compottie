package io.github.alexzhirkevich.compottie.internal

import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.layers.Layer
import io.github.alexzhirkevich.compottie.internal.schema.layers.NullLayer
import io.github.alexzhirkevich.compottie.internal.schema.layers.ShapeLayer
import io.github.alexzhirkevich.compottie.internal.schema.layers.ImageLayer
import io.github.alexzhirkevich.compottie.internal.schema.shapes.EllipseShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.FillShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientFillShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientStrokeShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GroupShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.PathShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.PolystarShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.RectShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.RoundShape
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
internal val LottieJson by lazy {
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
        serializersModule = SerializersModule {

            // all this polymorphic stuff doesn't really do anything.
            // just referencing classes here to avoid them been tree-shaken by the proguard/webpack

            polymorphic(Layer::class) {
                subclass(ShapeLayer::class)
                subclass(NullLayer::class)
                subclass(ImageLayer::class)

                defaultDeserializer {
                    NullLayer.serializer()
                }
            }

            polymorphic(Shape::class) {
                subclass(EllipseShape::class)
                subclass(FillShape::class)
                subclass(GradientFillShape::class)
                subclass(GradientStrokeShape::class)
                subclass(GroupShape::class)
                subclass(PathShape::class)
                subclass(PolystarShape::class)
                subclass(RectShape::class)
                subclass(RoundShape::class)
                subclass(SolidStrokeShape::class)
                subclass(TransformShape::class)
                subclass(TrimPathShape::class)
            }

            polymorphic(AnimatedColor::class) {
                subclass(AnimatedColor.Default::class)
                subclass(AnimatedColor.Animated::class)
            }

            polymorphic(AnimatedShape::class) {
                subclass(AnimatedShape.Default::class)
                subclass(AnimatedShape.Animated::class)
            }

            polymorphic(AnimatedValue::class) {
                subclass(AnimatedValue.Default::class)
                subclass(AnimatedValue.Animated::class)
            }

            polymorphic(AnimatedVector2::class) {
                subclass(AnimatedVector2.Default::class)
                subclass(AnimatedVector2.Animated::class)
            }
        }
    }
}