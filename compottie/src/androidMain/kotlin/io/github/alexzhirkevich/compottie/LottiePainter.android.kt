package io.github.alexzhirkevich.compottie

import android.app.Activity
import android.app.Application
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver


@OptIn(InternalCompottieApi::class)
internal actual fun makeFontFamilyResolver() : FontFamily.Resolver {
    return createFontFamilyResolver(
        requireNotNull(Compottie.context){
            "Compottie failed to initialize"
        }
    )
}

internal actual fun mockFontFamilyResolver() : FontFamily.Resolver =
    createFontFamilyResolver(Application())

