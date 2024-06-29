package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.jetbrains.compose.resources.rememberResourceEnvironment

/**
 * Create and remember Compose resources [LottieFontManager]
 *
 * Warning: this manager uses internal Compose API on Android and should be considered unstable
 * */
@OptIn(ExperimentalResourceApi::class)
@Composable
@ExperimentalCompottieApi
fun rememberResourcesFontManager(
    font : (LottieFontSpec) -> FontResource?
) : LottieFontManager {
    val factory by rememberUpdatedState(font)

    val environment = rememberResourceEnvironment()

    val context = currentLottieContext()

    return remember(environment, context) {
        ResourcesFontManagerImpl(
            context = context,
            environment = environment,
            resource = { factory(it) }
        )
    }
}

@OptIn(InternalCompottieApi::class, ExperimentalResourceApi::class)
@ExperimentalCompottieApi
/**
 * Factory method to create Compose resources [LottieFontManager] from non-composable context.
 *
 * Use [rememberResourcesFontManager] to create it from composition.
 *
 * LottiePainter created with this font manager won't work with Android Studio preview.
 *
 * Warning: this manager uses internal Compose API on Android and should be considered unstable
 * */
fun ResourcesFontManager(
    environment: ResourceEnvironment = getSystemResourceEnvironment(),
    resource : (LottieFontSpec) -> FontResource?
) : LottieFontManager = ResourcesFontManagerImpl(
    context = Compottie.context,
    environment = environment,
    resource = resource
)

@OptIn(ExperimentalResourceApi::class)
private class ResourcesFontManagerImpl(
    private val context: LottieContext?,
    private val environment: ResourceEnvironment,
    private val resource : (LottieFontSpec) -> FontResource?
) : LottieFontManager {

    override suspend fun font(font: LottieFontSpec): Font? {
        val resource = resource(font) ?: return null
        return loadFont(context, environment, font, resource)
    }
}


@OptIn(ExperimentalResourceApi::class)
internal expect suspend fun loadFont(
    context : LottieContext?,
    environment: ResourceEnvironment,
    font: LottieFontSpec,
    resource: FontResource
) : Font