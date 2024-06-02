package io.github.alexzhirkevich.compottie.internal.content

interface GreedyContent {
    fun absorbContent(contents: MutableListIterator<Content>)
}