package io.github.alexzhirkevich.compottie.internal

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.assets.PrecompositionAsset
import io.github.alexzhirkevich.compottie.internal.effects.BlurEffect
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.NullLayer
import io.github.alexzhirkevich.compottie.internal.layers.ShapeLayer
import io.github.alexzhirkevich.compottie.internal.layers.ImageLayer
import io.github.alexzhirkevich.compottie.internal.layers.PrecompositionLayer
import io.github.alexzhirkevich.compottie.internal.layers.SolidColorLayer
import io.github.alexzhirkevich.compottie.internal.layers.TextLayer
import io.github.alexzhirkevich.compottie.internal.shapes.EllipseShape
import io.github.alexzhirkevich.compottie.internal.shapes.FillShape
import io.github.alexzhirkevich.compottie.internal.shapes.GradientFillShape
import io.github.alexzhirkevich.compottie.internal.shapes.GradientStrokeShape
import io.github.alexzhirkevich.compottie.internal.shapes.GroupShape
import io.github.alexzhirkevich.compottie.internal.shapes.MergePathsShape
import io.github.alexzhirkevich.compottie.internal.shapes.PathShape
import io.github.alexzhirkevich.compottie.internal.shapes.PolystarShape
import io.github.alexzhirkevich.compottie.internal.shapes.RectShape
import io.github.alexzhirkevich.compottie.internal.shapes.RepeaterShape
import io.github.alexzhirkevich.compottie.internal.shapes.RoundShape
import io.github.alexzhirkevich.compottie.internal.shapes.Shape
import io.github.alexzhirkevich.compottie.internal.shapes.SolidStrokeShape
import io.github.alexzhirkevich.compottie.internal.shapes.TransformShape
import io.github.alexzhirkevich.compottie.internal.shapes.TrimPathShape
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

            polymorphic(Layer::class) {
                subclass(ShapeLayer::class)
                subclass(NullLayer::class)
                subclass(ImageLayer::class)
                subclass(PrecompositionLayer::class)
                subclass(TextLayer::class)
                subclass(SolidColorLayer::class)

                defaultDeserializer {
                    NullLayer.serializer()
                }
            }

            polymorphic(LottieAsset::class){
                subclass(ImageAsset::class)
                subclass(PrecompositionAsset::class)

                defaultDeserializer {
                    LottieAsset.UnsupportedAsset.serializer()
                }
            }

            polymorphic(Shape::class) {
                subclass(EllipseShape::class)
                subclass(FillShape::class)
                subclass(GradientFillShape::class)
                subclass(GradientStrokeShape::class)
                subclass(GroupShape::class)
                subclass(MergePathsShape::class)
                subclass(PathShape::class)
                subclass(PolystarShape::class)
                subclass(RectShape::class)
                subclass(RepeaterShape::class)
                subclass(RoundShape::class)
                subclass(SolidStrokeShape::class)
                subclass(TransformShape::class)
                subclass(TrimPathShape::class)

                defaultDeserializer {
                    Shape.UnsupportedShape.serializer()
                }
            }

            polymorphic(LayerEffect::class){
                subclass(BlurEffect::class)

                defaultDeserializer {
                    LayerEffect.UnsupportedEffect.serializer()
                }
            }

            // the rest polymorphic stuff doesn't really do anything.
            // just referencing classes here to avoid them been tree-shaken by the proguard/webpack

            polymorphic(AnimatedColor::class) {
                subclass(AnimatedColor.Default::class)
                subclass(AnimatedColor.Animated::class)
            }

            polymorphic(AnimatedShape::class) {
                subclass(AnimatedShape.Default::class)
                subclass(AnimatedShape.Animated::class)
            }

            polymorphic(AnimatedNumber::class) {
                subclass(AnimatedNumber.Default::class)
                subclass(AnimatedNumber.Animated::class)
            }

            polymorphic(AnimatedVector2::class) {
                subclass(AnimatedVector2.Default::class)
                subclass(AnimatedVector2.Animated::class)
                subclass(AnimatedVector2.Split::class)
            }
        }
    }
}