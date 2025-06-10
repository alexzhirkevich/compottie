package io.github.alexzhirkevich.compottie.internal.platform

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(parameter = 0)
internal actual fun isAndroidAtMost(code : Int) : Boolean = Build.VERSION.SDK_INT <= code

internal actual fun isAndroidAtLeast(code: Int): Boolean {
    TODO("Not yet implemented")
}

internal actual val currentComposeBackend: ComposeBackend
    get() = TODO("Not yet implemented")