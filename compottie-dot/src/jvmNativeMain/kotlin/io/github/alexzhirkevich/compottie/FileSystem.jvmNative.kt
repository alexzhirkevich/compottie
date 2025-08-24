package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.SYSTEM

@InternalCompottieApi
public actual fun defaultFileSystem() : FileSystem = FileSystem.SYSTEM
