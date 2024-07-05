package io.github.alexzhirkevich.compottie.internal.utils

import platform.Foundation.NSDate
import platform.Foundation.date
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMs(): Long {
    return (NSDate.date().timeIntervalSince1970 * 1000).toLong()
}