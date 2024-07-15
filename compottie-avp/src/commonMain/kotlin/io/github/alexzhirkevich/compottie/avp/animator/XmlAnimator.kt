@file: Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.compottie.avp.AnimatedVector
import io.github.alexzhirkevich.compottie.avp.xml.parseObjectAnimators
import org.jetbrains.compose.resources.toXmlElement

//@Composable
//public fun rememberObjectAnimators(vararg keys : Any?, xml : suspend () -> ByteArray) : AnimatorSpec {
//    return rememberObjectAnimatorsByte(*keys, animatorsXml = xml)
//}
//
//@Composable
//public fun rememberObjectAnimators(vararg keys : Any?, xml : suspend () -> String) : AnimatorSpec {
//    return rememberObjectAnimatorsByte(*keys) {
//        xml().encodeToByteArray()
//    }
//}

@Composable
private fun rememberObjectAnimatorsByte(
    vararg keys : Any?,
    resource : String,
    readBytes : suspend (String) -> ByteArray,
) : AnimatorSpec {
    return remember(*keys) {
        object : AnimatorSpec {
            override suspend fun load(animatedVector: AnimatedVector): Map<String, ObjectAnimator<*, *>> {
                val animators = animatorsXml()
                    .toXmlElement()
                    .parseObjectAnimators()

                animatedVector.animations.mapNotNull {
                    it.
                }
            }
        }
    }
}
