package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public fun rememberObjectAnimator(xml : suspend () -> String) : AnimatorSpec {
    return remember {
        object : AnimatorSpec {
            override suspend fun load(): ObjectAnimator<*, *> {
                xml().encodeToByteArray().
            }
        }
    }
}

@Composable
public fun rememberObjectAnimator(xml : ByteArray) : AnimatorSpec {
    l
}

