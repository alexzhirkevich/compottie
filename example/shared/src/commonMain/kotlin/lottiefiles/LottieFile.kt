package lottiefiles

import kotlinx.serialization.Serializable

@Serializable
internal class LottieFile(
    val id : String,
    val name : String? = null,
    val tags : List<String> = emptyList(),
    val lottieSource : String? = null,
    val jsonSource : String? = null,
    val bgColor : String? = null,
    val downloadCount : Int = 0,
    val slug : String = "",
    val hash : String = "",
    val user : User = User()
)

@Serializable
internal class User(
    val name : String? = null,
    val avatarUrl : String? = null,
    val username : String? = null
)
