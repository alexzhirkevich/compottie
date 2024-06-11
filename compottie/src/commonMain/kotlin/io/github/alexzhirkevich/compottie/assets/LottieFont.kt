package io.github.alexzhirkevich.compottie.assets

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.alexzhirkevich.compottie.internal.assets.LottieFontAsset

class LottieFont(
    val family : String,
    val name : String,
    val style : FontStyle,
    val weight : FontWeight,
    val path : String?
)