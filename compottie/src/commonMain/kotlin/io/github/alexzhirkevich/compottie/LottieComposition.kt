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
import io.github.alexzhirkevich.compottie.internal.durationMillis
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


@Stable
class LottieComposition internal constructor(
    internal val lottieData: LottieData,
) {
    val startFrame: Float get() = lottieData.inPoint

    val endFrame: Float get() = lottieData.outPoint

    val duration: Float get() = lottieData.durationMillis

    val frameRate: Float get() = lottieData.frameRate

    @InternalCompottieApi
    var iterations: Int by mutableStateOf(1)

    @InternalCompottieApi
    var speed: Float by mutableFloatStateOf(1f)

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
        fun parse(json: String) = LottieComposition(
            lottieData = LottieJson.decodeFromString(json),
        )
    }
}

/**
 * Load and prepare [LottieComposition].
 *
 * [spec] should be remembered
 * */
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
@Composable
@Stable
fun rememberLottieComposition(
    vararg keys : Any?,
    spec : suspend () -> LottieCompositionSpec,
) : LottieCompositionResult {

    val updatedSpec by rememberUpdatedState(spec)

    val result = remember(*keys) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result) {
        withContext(Dispatchers.IODispatcher) {
            try {
                result.complete(updatedSpec().load())
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
    }

    return result
}
