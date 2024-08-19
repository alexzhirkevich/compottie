package io.github.alexzhirkevich.compottie.network

import androidx.compose.ui.text.font.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import okio.Path

private const val CACHE_FONT_DIR = "compottie_font_cache"

internal actual suspend fun makeFont(spec: LottieFontSpec, path: Path, bytes: ByteArray) : Font {

//    val cacheFile = lottieContext.cacheDir.resolve(CACHE_FONT_DIR).resolve(path.name)
//    if (!cacheFile.exists()){
//        path.toFile().copyTo(cacheFile)
//    }
    return Font(
        file = path.toFile(),
        weight = spec.weight,
        style = spec.style
    )
}