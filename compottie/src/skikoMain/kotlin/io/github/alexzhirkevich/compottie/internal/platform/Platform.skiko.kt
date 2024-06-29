package io.github.alexzhirkevich.compottie.internal.platform

internal actual fun isAndroidAtLeast(code : Int) : Boolean = false

internal actual fun isAndroidAtMost(code : Int) : Boolean = false

internal actual val currentComposeBackend : ComposeBackend
    get() = ComposeBackend.Skiko