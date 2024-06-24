package io.github.alexzhirkevich.compottie

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

lateinit var lottieContext: Context

actual typealias LottieContext = Context

@Composable
actual fun currentLottieContext() : LottieContext {
    return LocalContext.current
}