package io.github.alexzhirkevich.compottie

import okio.FileSystem
import okio.fakefilesystem.FakeFileSystem

internal actual fun defaultFileSystem() : FileSystem = FakeFileSystem()