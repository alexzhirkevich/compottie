package io.github.alexzhirkevich.compottie.internal.utils

import kotlin.js.Date

internal actual fun currentTimeMs(): Long = Date.now().toLong()