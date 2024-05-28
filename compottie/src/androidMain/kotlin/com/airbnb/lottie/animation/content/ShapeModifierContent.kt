package com.airbnb.lottie.animation.content

import com.airbnb.lottie.model.content.ShapeData

interface ShapeModifierContent : Content {

    fun modifyShape(shapeData: ShapeData?): ShapeData?
}
