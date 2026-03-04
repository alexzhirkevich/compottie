package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.AnimationTheme
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import kotlinx.serialization.Serializable

@Serializable
internal class ThemeRules(
    val rules : List<ThemeRule<*>>
)

internal fun ThemeRules.toTheme() : AnimationTheme {
    return AnimationTheme(
        rules = rules.filterIsInstance<PropertyThemeRule<*>>().associate {
            it.id to it.property()
        },
        images = rules.filterIsInstance<ImageRule>().associate {
            it.id to ImageAsset(
                id = it.id,
                fileName = it.value?.url.orEmpty(),
                embedded = false,
                w = it.value?.width,
                h = it.value?.height
            )
        }
    )
}

@Serializable
internal sealed interface ThemeRule<V> {
    val id : String
    val value : V?
}

@Serializable
internal sealed interface PropertyThemeRule<V> : ThemeRule<V> {

    val expression : String?
    val keyframes : List<DotKeyframe<*>>?

    fun property() : RawProperty<*>
}