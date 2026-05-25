@file:Suppress("INVISIBLE_MEMBER","INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getResourceItemByEnvironment

@OptIn(InternalResourceApi::class)
internal actual suspend fun loadFont(
    context: LottieContext,
    environment: ResourceEnvironment,
    font: LottieFontSpec,
    resource: FontResource
) : Font = Font(
    path =  resource.getResourceItemByEnvironment(environment).path,
    assetManager = context.assets,
    weight = font.weight,
    style = font.style
)