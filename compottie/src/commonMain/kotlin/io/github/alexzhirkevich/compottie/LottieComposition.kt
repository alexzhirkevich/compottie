package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.assets.LottieAsset
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.internal.LottieData
import io.github.alexzhirkevich.compottie.internal.LottieJson
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import io.github.alexzhirkevich.compottie.internal.durationMillis
import io.github.alexzhirkevich.compottie.internal.platform.fromBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.JvmInline


@Stable
class LottieComposition internal constructor(
    internal val lottieData: LottieData,
    internal val fonts : Map<String, FontFamily> = emptyMap(),
) {
    val startFrame: Float get() = lottieData.inPoint

    val endFrame: Float get() = lottieData.outPoint

    val duration: Float get() = lottieData.durationMillis

    val frameRate: Float get() = lottieData.frameRate

    @InternalCompottieApi
    var iterations : Int by mutableStateOf(1)

    @InternalCompottieApi
    var speed : Float by mutableFloatStateOf(1f)

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
            lottieData.assets.map { asset ->
                launch(Dispatchers.Default) {
                    when (asset) {
                        is ImageAsset -> {
                            if (asset.bitmap == null) {
                                assetsManager.fetch(
                                    LottieAsset(
                                        id = asset.id,
                                        type = LottieAsset.AssetType.Image,
                                        path = asset.path,
                                        name = asset.fileName
                                    )
                                )?.let {
                                    asset.setBitmap(ImageBitmap.fromBytes(it))
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }.joinAll()
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
 * Load and prepare [LottieComposition] for displaying
 * */
@Composable
@Stable
fun rememberLottieComposition(
    spec : LottieCompositionSpec,
    assetsManager: LottieAssetsManager = LottieAssetsManager,
) : LottieCompositionResult {

    val result = remember(spec) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(result) {
        withContext(Dispatchers.Default) {
            try {
                result.complete(spec.load().apply { prepare(assetsManager) })
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
    }

    return result
}




@Immutable
@JvmInline
internal value class JsonStringCompositionSpec(
    private val jsonString: String
) : LottieCompositionSpec {

    override suspend fun load(): LottieComposition {
        return LottieComposition.parse(jsonString)
    }

    override fun toString(): String {
        return "JsonString(jsonString='$jsonString')"
    }
}
