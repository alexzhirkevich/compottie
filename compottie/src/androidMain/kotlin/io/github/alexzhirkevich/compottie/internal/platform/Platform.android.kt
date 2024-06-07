package io.github.alexzhirkevich.compottie.internal.platform

import android.graphics.RenderEffect
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.ui.graphics.ColorFilter

@ChecksSdkIntAtLeast(parameter = 0)
internal actual fun isAndroidAtLeast(code : Int) : Boolean = Build.VERSION.SDK_INT >= code

fun foo(){
    ColorFilter
}

@ChecksSdkIntAtLeast(parameter = 0)
internal actual fun isAndroidAtMost(code : Int) : Boolean = Build.VERSION.SDK_INT <= code
