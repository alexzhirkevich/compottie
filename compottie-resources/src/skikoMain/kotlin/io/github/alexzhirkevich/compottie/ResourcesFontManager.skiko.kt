package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getFontResourceBytes

@OptIn(ExperimentalResourceApi::class)
internal actual suspend fun loadFont(
    context : LottieContext?,
    environment: ResourceEnvironment,
    font: LottieFontSpec,
    resource: FontResource
) : Font {
    return androidx.compose.ui.text.platform.Font(
        identity = "${font.family}_${font.weight.weight}_${font.style}",
        data = getFontResourceBytes(environment, resource),
        weight = font.weight,
        style = font.style
    )
}