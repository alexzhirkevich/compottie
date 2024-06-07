package io.github.alexzhirkevich.compottie.assets

import androidx.compose.runtime.Immutable

@Immutable
class LottieAsset internal constructor(
    val id : String,
    val type : AssetType,
    val path: String,
    val name : String
) {
    enum class AssetType {
        Image
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LottieAsset

        if (id != other.id) return false
        if (type != other.type) return false
        if (path != other.path) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "LottieAsset(id='$id', type=$type, path='$path', name='$name')"
    }
}