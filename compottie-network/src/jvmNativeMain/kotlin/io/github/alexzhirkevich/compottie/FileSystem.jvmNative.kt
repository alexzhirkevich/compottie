package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.SYSTEM

internal actual fun defaultFileSystem() : FileSystem = FileSystem.SYSTEM