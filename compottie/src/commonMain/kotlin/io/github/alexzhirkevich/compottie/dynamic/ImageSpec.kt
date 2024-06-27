package io.github.alexzhirkevich.compottie.dynamic

class ImageSpec internal constructor(
    val id : String,
    val path : String,
    val name : String,
    val width : Int,
    val height : Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageSpec

        if (id != other.id) return false
        if (path != other.path) return false
        if (name != other.name) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }

    override fun toString(): String {
        return "ImageSpec(id='$id', path='$path', name='$name', width=$width, height=$height)"
    }
}