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
import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFontManager
import io.github.alexzhirkevich.compottie.internal.Animation
import io.github.alexzhirkevich.compottie.internal.LottieJson
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionCompositionFromAsset
import io.github.alexzhirkevich.compottie.internal.assets.CharacterData
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.assets.PrecompositionAsset
import io.github.alexzhirkevich.compottie.internal.helpers.Marker
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds


/**
 * Load and prepare [LottieComposition] for displaying.
 *
 * Remembers the composition produced by [spec] if all values of [keys] are equal (`==`) to the
 * values they had in the previous composition, otherwise produce and remember a new [LottieCompositionResult] by
 * calling [spec] again.
 * */
@OptIn(InternalCompottieApi::class)
@Composable
public fun rememberLottieComposition(
    vararg keys: Any?,
    cache: LottieCompositionCache? = LocalLottieCache.current,
    spec : suspend () -> LottieCompositionSpec,
) : LottieCompositionResult {

    val result = remember(*keys, cache) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result, cache) {
        try {
            val composition = withContext(Compottie.ioDispatcher()) {
                val specInstance = spec()
                cache?.getOrPut(specInstance.key, specInstance::load) ?: specInstance.load()
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
 * Load [LottieComposition].
 * */
@OptIn(InternalCompottieApi::class)
@Deprecated(
    "Use overload with lambda instead",
    ReplaceWith("rememberLottieComposition { spec }")
)
@Composable
public fun rememberLottieComposition(
    spec : LottieCompositionSpec,
) : LottieCompositionResult {

    val result = remember(spec) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result) {

        try {
            val composition = withContext(Compottie.ioDispatcher()) { spec.load() }
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

@Stable
public class LottieComposition internal constructor(
    internal val animation: Animation,
) {

    public companion object {
        public fun parse(json: String): LottieComposition {
            return LottieComposition(LottieJson.decodeFromString(json))
        }
    }

    /**
     * Frame when animation becomes visible
     * */
    public val startFrame: Float get() = animation.inPoint

    /**
     * Frame when animation becomes no longer visible
     * */
    public val endFrame: Float get() = animation.outPoint

    /**
     * Animation duration
     * */
    public val duration: Duration = (durationFrames / frameRate * 1_000_000).toInt().microseconds

    public val durationFrames : Float
        get() = animation.outPoint - animation.inPoint

    /**
     * Animation start time in seconds
     * */
    public val startTime : Float
        get() = animation.inPoint / animation.frameRate

    /**
     * Animation frame rate
     * */
    public val frameRate: Float get() = animation.frameRate

    /**
     * Animation intrinsic width
     * */
    public val width: Float get() = animation.width

    /**
     * Animation intrinsic height
     * */
    public val height: Float get() = animation.height

    /**
     * Some animations may contain predefined number of interactions.
     * It will be used as a default value for the LottiePainter
     * */
    public var iterations: Int by mutableStateOf(1)
        @InternalCompottieApi
        set

    /**
     * Some animations may contain predefined speed multiplier.
     * It will be used as a default value for the LottiePainter
     * */
    public var speed: Float by mutableFloatStateOf(1f)
        @InternalCompottieApi
        set

    internal val expressionComposition = object : ExpressionComposition {

        override val name: String?
            get() = animation.name
        override val width: Float
            get() = this@LottieComposition.width
        override val height: Float
            get() = this@LottieComposition.height
        override val startTime: Float
            get() = this@LottieComposition.startTime

        override val layersByName: Map<String, Layer> by lazy {
            animation.layers.associateBy { it.name.orEmpty() }
        }

        override val layersByIndex: Map<Int, Layer> by lazy {
            animation.layers.associateBy { it.index ?: Int.MIN_VALUE }
        }

        override val layersCount: Int
            get() = animation.layers.size
    }

    private val charGlyphs: Map<String, Map<String, CharacterData>> =
        animation.chars
            .groupBy(CharacterData::fontFamily)
            .mapValues { it.value.associateBy(CharacterData::character) }

    internal val precomps : Map<String, ExpressionComposition> by lazy {
        animation.assets.filterIsInstance<PrecompositionAsset>()
            .associateBy { it.name.orEmpty() }
            .mapValues { ExpressionCompositionFromAsset(it.value) }
    }

    private val assetsMutex = Mutex()
    private val fontsMutex = Mutex()

    private var storedFonts : MutableMap<String, FontFamily> = mutableMapOf()

    private val markersMap = animation.markers.associateBy(Marker::name)

    internal fun findGlyphs(family : String?) : Map<String, CharacterData>? {
        return charGlyphs[family] ?: run {
            val font = animation.fonts?.list
                ?.find { it.name == family || it.family == family }
                ?: return@run null

            charGlyphs[font.family] ?: charGlyphs[font.name]
        }
    }


    @InternalCompottieApi
    public suspend fun prepareAssets(assetsManager: LottieAssetsManager) {
        assetsMutex.withLock {
            loadAssets(assetsManager, false)
        }
    }

    @InternalCompottieApi
    public suspend fun prepareFonts(fontsManager : LottieFontManager) {
        fontsMutex.withLock {
            storedFonts.putAll(loadFontsInternal(fontsManager))
        }
    }

    internal suspend fun loadAssets(
        assetsManager: LottieAssetsManager,
        copy : Boolean
    ) : List<LottieAsset> {
        val assets = if (copy)
            animation.assets.map(LottieAsset::copy)
        else animation.assets

        coroutineScope {
            assets.mapNotNull { asset ->
                when (asset) {
                    is ImageAsset -> {
                        if (asset.bitmap == null) {
                            launch(Dispatchers.Default) {
                                try {
                                    assetsManager.image(asset.spec)?.let {
                                        asset.setBitmap(it.toBitmap(asset.width, asset.height))
                                    }
                                } catch (t: CancellationException) {
                                    throw t
                                } catch (t: Throwable) {
                                    Compottie.logger?.error("Image asset failed to load: ${asset.name}", t)
                                }
                            }
                        } else null
                    }

                    else -> null
                }
            }.joinAll()
        }
        return assets
    }

    internal suspend fun loadFonts(fontManager: LottieFontManager) : Map<String, FontFamily> {
        return coroutineScope {
            storedFonts + loadFontsInternal(fontManager)
        }
    }

    private suspend fun loadFontsInternal(fontManager: LottieFontManager) : Map<String, FontFamily> {
        return coroutineScope {
            storedFonts + animation.fonts?.list
                ?.fastMap {
                    async {
                        try {
                            val f = it.font ?: fontManager.font(it.spec)

                            it.font = f

                            if (f == null)
                                null
                            else listOf(it.family to f, it.name to f)
                        } catch (t : CancellationException){
                            throw t
                        } catch (t : Throwable){
                            Compottie.logger?.error("Font asset failed to load: ${it.family} - ${it.name}", t)
                            null
                        }
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

    internal fun deepCopy() : LottieComposition {
        return LottieComposition(animation.deepCopy())
    }

    internal fun marker(name: String?) = markersMap[name]
}

private object UnspecifiedCompositionKey