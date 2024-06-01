package io.github.alexzhirkevich.compottie.internal.platform

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(parameter = 0)
internal actual fun isAndroidAtLeast(code : Int) : Boolean = Build.VERSION.SDK_INT >= code


@ChecksSdkIntAtLeast(parameter = 0)
internal actual fun isAndroidAtMost(code : Int) : Boolean = Build.VERSION.SDK_INT <= code
