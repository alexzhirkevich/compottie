@file:Suppress("INVISIBLE_MEMBER","INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFont
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getFontResourceBytes
import org.jetbrains.compose.resources.getResourceItemByEnvironment

@OptIn(ExperimentalResourceApi::class, InternalCompottieApi::class, InternalResourceApi::class)
internal actual suspend fun loadFont(
    environment: ResourceEnvironment,
    font: LottieFont,
    resource: FontResource
) : Font {
    val path =  resource.getResourceItemByEnvironment(environment).path
    return Font(path, L.context.assets, font.weight, font.style)
}