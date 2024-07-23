package lottiefiles

import kotlinx.serialization.Serializable

@Serializable
internal class Suggestion(
    val query : String,
    val id : String,
    val popularity : Int
)