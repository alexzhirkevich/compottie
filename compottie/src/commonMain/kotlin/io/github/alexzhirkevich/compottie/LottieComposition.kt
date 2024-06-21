package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.internal.Animation
import io.github.alexzhirkevich.compottie.internal.LottieJson
import io.github.alexzhirkevich.compottie.internal.assets.CharacterData
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextDocument
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

/**
 * Load and prepare [LottieComposition] for displaying.
 *
 * Instance produces by [spec] will be remembered until [key] is changed. Those instances
 * are cached across the whole application. Cache size can be configured with [L.compositionCacheLimit]
 *
 * You can configure various dynamic animation properties in the [dynamic] block.
 * */
@OptIn(InternalCompottieApi::class)
@Composable
@Stable
fun rememberLottieComposition(
    key : Any? = currentCompositeKeyHash,
    assetsManager: LottieAssetsManager = LottieAssetsManager.Empty,
    fontManager: LottieFontManager = LottieFontManager.Empty,
    spec : suspend () -> LottieCompositionSpec,
) : LottieCompositionResult {

    val updatedSpec by rememberUpdatedState(spec)

    val result = remember(key, assetsManager) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result) {
        try {
            val composition = withContext(ioDispatcher()) {
                updatedSpec().load(key).apply {
                    prepare(
                        assetsManager = assetsManager,
                        fontManager = fontManager
                    )
                }
            }
            result.complete(composition)
        } catch (c: CancellationException) {
            result.completeExceptionally(c)
            throw c
        } catch (t: Throwable) {
            result.completeExceptionally(
                CompottieException("Composition failed to load", t)
            )
        }
    }

    return result
}

/**
 * Load and prepare [LottieComposition].
 * */
@Deprecated(
    "Use overload with lambda instead",
    ReplaceWith("rememberLottieComposition { spec }")
)
@OptIn(InternalCompottieApi::class)
@Composable
@Stable
fun rememberLottieComposition(
    spec : LottieCompositionSpec,
) : LottieCompositionResult =rememberLottieComposition { spec }

@Stable
class LottieComposition internal constructor(
    internal val animation: Animation,
) {

    /**
     * Frame when animation becomes visible
     * */
    val startFrame: Float get() = animation.inPoint

    /**
     * Frame when animation becomes no longer visible
     * */
    val endFrame: Float get() = animation.outPoint

    /**
     * Animation duration
     * */
    val duration: Duration = ((endFrame - startFrame) / frameRate * 1000).toInt().milliseconds

    /**
     * Animation frame rate
     * */
    val frameRate: Float get() = animation.frameRate

    /**
     * Animation intrinsic width
     * */
    val width: Float get() = animation.width

    /**
     * Animation intrinsic height
     * */
    val height: Float get() = animation.height

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

    private var fontsByFamily: Map<String, FontFamily> = emptyMap()

    private val prepareMutex = Mutex()

    private val charGlyphs: Map<String, Map<String, CharacterData>> =
        animation.chars
            .groupBy(CharacterData::fontFamily)
            .mapValues { it.value.associateBy(CharacterData::character) }

    internal fun findFont(family: String?) : FontFamily? {
        return fontsByFamily[family]
    }

    internal fun findGlyphs(family : String?) : Map<String, CharacterData>? {
        return charGlyphs[family] ?: run {
            val font = animation.fonts?.list
                ?.find { it.name == family || it.family == family }
                ?: return@run null

            charGlyphs[font.family] ?: charGlyphs[font.name]
        }
    }

    private var isPrepared = false

    /**
     * Preload assets for instant animation displaying.
     *
     * Assets that are already loaded (such as embedded base64 images or assets
     * successfully loaded at prev [prepare] call) will not be loaded again
     * */
    suspend fun prepare(
        assetsManager: LottieAssetsManager = LottieAssetsManager.Empty,
        fontManager: LottieFontManager = LottieFontManager.Empty
    ) {
        prepareMutex.withLock {
//            if (!isPrepared) {
                coroutineScope {
                    launch {
                        loadAssets(assetsManager)
                    }
                    launch {
                        loadFonts(fontManager)
                    }
                }.invokeOnCompletion {
                    isPrepared = it == null
                }
//            }
        }
    }

    private suspend fun loadAssets(assetsManager: LottieAssetsManager) {
        coroutineScope {
            animation.assets.mapNotNull { asset ->
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


    private suspend fun loadFonts(assetsManager: LottieFontManager) {
        coroutineScope {
            fontsByFamily = animation.fonts?.list
                ?.map {
                    async {
                        val f = it.font ?: assetsManager.font(
                            LottieFontSpec(
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
                        else listOf(it.family to f, it.name to f)
                    }
                }
                ?.awaitAll()
                ?.filterNotNull()
                ?.flatten()
                ?.groupBy { it.first }
                ?.filterValues { it.isNotEmpty() }
                ?.mapValues { FontFamily(it.value.map { it.second }) }
                .orEmpty()
        }
    }

    internal fun marker(name: String?) =
        animation.markers.firstOrNull { it.name == name }

    companion object {

        fun parse(json: String): LottieComposition {
            return LottieComposition(
                animation = LottieJson.decodeFromString(json),
            )
        }

        /**
         * Get cached composition for [key] or create new one and cache it by [key]
         * */
        suspend fun getOrCreate(key : Any?, create : suspend () -> LottieComposition) : LottieComposition {
            if (key == null)
                return create()

            return cache.getOrPutSuspend(key, create)
        }

        /**
         * Clear all previously cached compositions
         * */
        fun clearCache() = cache.clear()

        @OptIn(ExperimentalCompottieApi::class)
        private val cache = LruMap<LottieComposition>(limit = L::compositionCacheLimit)
    }
}