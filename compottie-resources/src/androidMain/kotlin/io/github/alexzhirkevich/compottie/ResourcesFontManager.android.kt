@file:Suppress("INVISIBLE_MEMBER","INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getResourceItemByEnvironment

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
internal actual suspend fun loadFont(
    context : LottieContext,
    environment: ResourceEnvironment,
    font: LottieFontSpec,
    resource: FontResource
) : Font {
    val path =  resource.getResourceItemByEnvironment(environment).path
    return Font(path, context.assets, font.weight, font.style)
}