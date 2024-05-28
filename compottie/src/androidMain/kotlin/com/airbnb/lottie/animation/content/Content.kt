package com.airbnb.lottie.animation.content

interface Content {
    val name: String?

    fun setContents(contentsBefore: List<Content?>?, contentsAfter: List<Content?>?)
}
