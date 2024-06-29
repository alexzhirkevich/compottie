package io.github.alexzhirkevich.compottie

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.platform.Font
import io.github.alexzhirkevich.compottie.assets.LottieFontSpec
import okio.Path

internal actual suspend fun makeFont(spec: LottieFontSpec, path: Path, bytes: ByteArray) : Font {
    return Font(
        identity = spec.name,
        getData = { bytes },
        weight = spec.weight,
        style = spec.style
    )
}