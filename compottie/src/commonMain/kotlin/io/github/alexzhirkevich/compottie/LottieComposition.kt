package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFont
import io.github.alexzhirkevich.compottie.internal.LottieData
import io.github.alexzhirkevich.compottie.internal.LottieJson
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


@Stable
class LottieComposition internal constructor(
    internal val lottieData: LottieData,
) {

    /**
     * Frame when animation becomes visible
     * */
    val startFrame: Float get() = lottieData.inPoint

    /**
     * Frame when animation becomes no longer visible
     * */
    val endFrame: Float get() = lottieData.outPoint

    /**
     * Animation duration
     * */
    val duration: Duration = ((endFrame - startFrame) / frameRate * 1000).toInt().milliseconds

    /**
     * Animation frame rate
     * */
    val frameRate: Float get() = lottieData.frameRate

    /**
     * Some animations may contain predefined number of interactions.
     * It will be used as a default value for the LottiePainter
     * */
    var iterations: Int by mutableStateOf(1)
        @InternalCompottieApi
        set

    /**
     * Some animations may contain predefined speed multiplier.
     * It will be used as a default value for the LottiePainter
     * */
    var speed: Float by mutableFloatStateOf(1f)
        @InternalCompottieApi
        set

    internal var fontsByFamily: Map<String, FontFamily> = emptyMap()

    private val fontMutex = Mutex()

    /**
     * Preload assets for instant animation displaying.
     *
     * Assets that are already loaded (such as embedded base64 images or assets
     * successfully loaded at prev [prepare] call) will not be loaded again
     * */
    suspend fun prepare(
        assetsManager: LottieAssetsManager
    ) {
        coroutineScope {
            launch {
                loadAssets(assetsManager)
            }
            launch {
                loadFonts(assetsManager)
            }
        }
    }

    private suspend fun loadAssets(assetsManager: LottieAssetsManager) {
        coroutineScope {
            lottieData.assets.mapNotNull { asset ->
                when (asset) {
                    is ImageAsset -> {
                        if (asset.bitmap == null) {
                            launch(Dispatchers.Default) {
                                assetsManager.image(
                                    LottieImage(
                                        id = asset.id,
                                        path = asset.path,
                                        name = asset.fileName
                                    )
                                )?.let {
                                    asset.setBitmap(it.toBitmap(asset.width, asset.height))
                                }
                            }
                        } else null
                    }

                    else -> null
                }
            }.joinAll()
        }
    }


    private suspend fun loadFonts(assetsManager: LottieAssetsManager) {
        fontMutex.withLock {
            coroutineScope {
                fontsByFamily = lottieData.fonts?.list
                    ?.map {
                        async {
                            val f = it.font ?: assetsManager.font(
                                LottieFont(
                                    family = it.family,
                                    name = it.name,
                                    style = it.fontStyle,
                                    weight = it.weight,
                                    path = it.path
                                )
                            )

                            it.font = f
                            if (f == null)
                                null
                            else it.family to f
                        }
                    }
                    ?.awaitAll()
                    ?.filterNotNull()
                    ?.groupBy { it.first }
                    ?.filterValues { it.isNotEmpty() }
                    ?.mapValues { FontFamily(it.value.map { it.second }) }
                    .orEmpty()
            }
        }
    }

    internal fun marker(name: String?) =
        lottieData.markers.firstOrNull { it.name == name }

    companion object {
        fun parse(json: String) : LottieComposition {
            return LottieComposition(
                lottieData = LottieJson.decodeFromString(json),
            )
        }
    }
}

/**
 * Load and prepare [LottieComposition].
 *
 * [spec] should be remembered
 * */
@OptIn(InternalCompottieApi::class)
@Composable
@Stable
fun rememberLottieComposition(
    spec : LottieCompositionSpec,
) : LottieCompositionResult {

    val result = remember(spec) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result) {
        withContext(Dispatchers.IODispatcher) {
            try {
                result.complete(spec.load())
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
    }

    return result
}

/**
 * Load and prepare [LottieComposition] for displaying.
 *
 * Instance produces by [spec] will be remembered until [keys] are changed
 * */
@OptIn(InternalCompottieApi::class)
@Composable
@Stable
fun rememberLottieComposition(
    vararg keys : Any?,
    spec : suspend (LottieContext) -> LottieCompositionSpec,
) : LottieCompositionResult {

    val updatedSpec by rememberUpdatedState(spec)

    val context = currentLottieContext()

    val result = remember(*keys,context) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result) {
        withContext(Dispatchers.IODispatcher) {
            try {
                result.complete(updatedSpec(context).load())
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
    }

    return result
}
