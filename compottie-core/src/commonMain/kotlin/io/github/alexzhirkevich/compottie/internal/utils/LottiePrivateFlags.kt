package io.github.alexzhirkevich.compottie.internal.utils

import io.github.alexzhirkevich.compottie.internal.LottieJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/*
 * lottie-web compatibility for animation data that was dumped AFTER lottie-web
 * processed it (DataManager.completeData).
 *
 * lottie-web converts bezier in/out tangents from relative (Lottie spec) to
 * absolute coordinates in place and marks the data:
 *   - root  "__complete": true  -> whole animation (incl. assets) was converted
 *   - layer "completed":  true  -> that layer (shapes + masks) was converted
 * and it skips the conversion for flagged data. Compottie follows the spec and
 * always treats tangents as relative, so flagged data must be converted back.
 *
 * The same pass also:
 *   - removes the flags (so the data can't be converted twice);
 *   - tolerates trailing commas;
 *   - fills a missing "s" of the last keyframe from the previous keyframe's "e"
 *     (old exporters omit it, lottie-web copes, e.g. `{"t": 32}`).
 *
 * Data without flags is returned untouched.
 */
internal fun normalizeLottiePrivateFlags(json: String): JsonElement? {
    if ("\"__complete\":" !in json && "\"completed\":" !in json)
        return null

    val root = runCatching { LottieJson.parseToJsonElement(json) }
        .getOrNull() as? JsonObject ?: return null

    return runCatching {
        root
            .fixRoot(root["__complete"].isTrue())
            .fillLastKeyframes()
    }.getOrElse { root }
}

private fun JsonElement?.isTrue(): Boolean =
    (this as? JsonPrimitive)?.booleanOrNull == true

private fun JsonObject.fixRoot(rootComplete: Boolean): JsonObject {
    val result = toMutableMap()
    result.remove("__complete")

    (this["layers"] as? JsonArray)?.let { result["layers"] = it.fixLayers(rootComplete) }

    (this["assets"] as? JsonArray)?.let { assets ->
        result["assets"] = JsonArray(assets.map { asset ->
            val layers = (asset as? JsonObject)?.get("layers") as? JsonArray
            if (asset is JsonObject && layers != null) {
                JsonObject(asset + ("layers" to layers.fixLayers(rootComplete)))
            } else {
                asset
            }
        })
    }
    return JsonObject(result)
}

private fun JsonArray.fixLayers(rootComplete: Boolean): JsonArray = JsonArray(map { layer ->
    if (layer !is JsonObject) return@map layer

    val flagged = rootComplete || layer["completed"].isTrue()
    val result = layer.toMutableMap()
    result.remove("completed")

    // lottie-web may have inlined precomp layers into the layer itself
    (layer["layers"] as? JsonArray)?.let { result["layers"] = it.fixLayers(rootComplete) }

    if (flagged) {
        (layer["shapes"] as? JsonArray)?.let { result["shapes"] = it.fixShapes() }

        (layer["masksProperties"] as? JsonArray)?.let { masks ->
            result["masksProperties"] = JsonArray(masks.map { mask ->
                val pt = (mask as? JsonObject)?.get("pt") as? JsonObject
                if (mask is JsonObject && pt != null) {
                    JsonObject(mask + ("pt" to pt.fixPathProperty()))
                } else {
                    mask
                }
            })
        }
    }
    JsonObject(result)
})

private fun JsonArray.fixShapes(): JsonArray = JsonArray(map { shape ->
    if (shape !is JsonObject) return@map shape

    when ((shape["ty"] as? JsonPrimitive)?.contentOrNull) {
        "sh" -> (shape["ks"] as? JsonObject)
            ?.let { JsonObject(shape + ("ks" to it.fixPathProperty())) }
            ?: shape

        "gr" -> (shape["it"] as? JsonArray)
            ?.let { JsonObject(shape + ("it" to it.fixShapes())) }
            ?: shape

        else -> shape
    }
})

/** Animatable path property: {"a":..,"k": <bezier | keyframes>} */
private fun JsonObject.fixPathProperty(): JsonObject {
    val k = this["k"] ?: return this
    return JsonObject(this + ("k" to k.fixPathValue()))
}

private fun JsonElement.fixPathValue(): JsonElement = when (this) {
    is JsonObject -> if ("v" in this) relativize() else this

    is JsonArray -> JsonArray(map { keyframe ->
        if (keyframe !is JsonObject) return@map keyframe
        val result = keyframe.toMutableMap()
        for (key in listOf("s", "e")) {
            when (val value = keyframe[key]) {
                is JsonArray -> result[key] = JsonArray(value.map { bezier ->
                    if (bezier is JsonObject && "v" in bezier) bezier.relativize() else bezier
                })

                is JsonObject -> if ("v" in value) result[key] = value.relativize()
                else -> Unit
            }
        }
        JsonObject(result)
    })

    else -> this
}

/** absolute in/out tangents -> relative to their vertex (inverse of lottie-web convertPathsToAbsoluteValues) */
private fun JsonObject.relativize(): JsonObject {
    val v = this["v"] as? JsonArray ?: return this
    val i = this["i"] as? JsonArray ?: return this
    val o = this["o"] as? JsonArray ?: return this

    fun subtractVertices(tangents: JsonArray): JsonArray = JsonArray(tangents.mapIndexed { n, point ->
        val vertex = v.getOrNull(n) as? JsonArray
        val tangent = point as? JsonArray
        if (vertex == null || tangent == null) {
            point
        } else {
            JsonArray(tangent.mapIndexed { d, component ->
                val base = (vertex.getOrNull(d) as? JsonPrimitive)?.doubleOrNull
                val value = (component as? JsonPrimitive)?.doubleOrNull
                if (base == null || value == null) component else JsonPrimitive(value - base)
            })
        }
    })

    return JsonObject(this + mapOf("i" to subtractVertices(i), "o" to subtractVertices(o)))
}

private fun JsonElement.fillLastKeyframes(): JsonElement = when (this) {
    is JsonObject -> JsonObject(mapValues { (key, value) ->
        val child = value.fillLastKeyframes()
        if (key == "k" && child is JsonArray) child.withLastKeyframeFilled() else child
    })

    is JsonArray -> JsonArray(map { it.fillLastKeyframes() })
    else -> this
}

private fun JsonArray.withLastKeyframeFilled(): JsonArray {
    if (this.size < 2) return this

    val lastKeyframe = this[this.size - 1] as? JsonObject ?: return this
    val previousKeyframe = this[this.size - 2] as? JsonObject ?: return this

    if (!lastKeyframe.containsKey("t") || lastKeyframe.containsKey("s")) return this

    val fill = previousKeyframe["e"] ?: return this

    val patchedLast = JsonObject(lastKeyframe.toMutableMap().apply { put("s", fill) })
    val result = this.toMutableList()
    result[result.size - 1] = patchedLast
    return JsonArray(result)
}