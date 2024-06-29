package io.github.alexzhirkevich.compottie.internal.platform

internal expect fun isAndroidAtLeast(code : Int) : Boolean
internal expect fun isAndroidAtMost(code : Int) : Boolean

internal expect val currentComposeBackend : ComposeBackend
internal enum class ComposeBackend {
    Android, Skiko
}