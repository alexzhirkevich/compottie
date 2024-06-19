package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFont
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.rememberResourceEnvironment

@OptIn(ExperimentalResourceApi::class)
@Composable
@ExperimentalCompottieApi
fun rememberResourcesFontManager(
    font : (LottieFont) -> FontResource?
) : LottieFontManager {
    val factory by rememberUpdatedState(font)

    val environment = rememberResourceEnvironment()

    return remember(environment) {
        ResourcesFontManager(
            environment = environment,
            resource = { factory(it) }
        )
    }
}


@OptIn(ExperimentalResourceApi::class)
private class ResourcesFontManager(
    private val environment: ResourceEnvironment,
    private val resource : (LottieFont) -> FontResource?
) : LottieFontManager {

    override suspend fun font(font: LottieFont): Font? {
        val resource = resource(font) ?: return null
        return loadFont(environment, font, resource)
    }
}


@OptIn(ExperimentalResourceApi::class)
internal expect suspend fun loadFont(
    environment: ResourceEnvironment,
    font: LottieFont,
    resource: FontResource
) : Font