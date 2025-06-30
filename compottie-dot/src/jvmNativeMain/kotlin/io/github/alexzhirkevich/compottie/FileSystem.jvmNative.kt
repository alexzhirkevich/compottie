package io.github.alexzhirkevich.compottie

import okio.FileSystem

@InternalCompottieApi
public actual fun defaultFileSystem() : FileSystem = FileSystem.SYSTEM
