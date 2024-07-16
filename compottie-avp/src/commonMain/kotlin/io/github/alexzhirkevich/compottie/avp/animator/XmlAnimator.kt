@file: Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.ui.unit.Density
import io.github.alexzhirkevich.compottie.avp.AnimatedImageVector
import io.github.alexzhirkevich.compottie.avp.xml.drawable
import io.github.alexzhirkevich.compottie.avp.xml.parseAnimationTargets
import io.github.alexzhirkevich.compottie.avp.xml.parseObjectAnimators
import io.github.alexzhirkevich.compottie.avp.xml.toAnimatedImageVector
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

private suspend fun loadXmlResourceVector(
    density: Density,
    resource : String,
    readBytes : suspend (String) -> ByteArray,
) : AnimatedImageVector {

    val xml = readBytes(resource)
        .toXmlElement()
    val drawable = readBytes(xml.drawable())

    val animators = coroutineScope {
        xml.parseAnimationTargets()
            .map {
                async {
                    it.name to readBytes(it.animation)
                        .toXmlElement()
                        .parseObjectAnimators()
                        .associateBy { it.property }
                }
            }
            .awaitAll()
            .toMap()
    }

    return drawable.toXmlElement().toAnimatedImageVector(
        density = density,
        animators = animators
    )
}
