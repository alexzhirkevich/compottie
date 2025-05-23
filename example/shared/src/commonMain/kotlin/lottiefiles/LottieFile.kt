package lottiefiles

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable
internal data class LottieFile(
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
) {
    var initialProgress by mutableStateOf(0f)
}

@Serializable
internal data class User(
    val name : String? = null,
    val avatarUrl : String? = null,
    val username : String? = null
)
