package io.github.alexzhirkevich.compottie.internal.content


interface Content {

    val name: String?

    fun setContents(
        contentsBefore: List<Content>,
        contentsAfter: List<Content>
    )
}
