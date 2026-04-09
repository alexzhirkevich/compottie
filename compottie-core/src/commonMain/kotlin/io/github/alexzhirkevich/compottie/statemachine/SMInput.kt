package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SMInput {

    public val name : String

    public fun assign(variables : MutableMap<String, Any>)

    @Serializable
    @SerialName("Numeric")
    public class Numeric(
        override val name : String,
        public val value : Float
    ) : SMInput {

        override fun assign(variables: MutableMap<String, Any>) {
            variables[name] = value
        }
    }

    @Serializable
    @SerialName("String")
    public class Str(
        override val name : String,
        public val value : String
    ) : SMInput {

        override fun assign(variables: MutableMap<String, Any>) {
            variables[name] = value
        }
    }

    @Serializable
    @SerialName("Boolean")
    public class Bool(
        override val name : String,
        public val value : Boolean
    ) : SMInput {

        override fun assign(variables: MutableMap<String, Any>) {
            variables[name] = value
        }
    }

    @Serializable
    @SerialName("Event")
    public class Event(
        override val name : String,
    ) : SMInput {

        internal var isTriggered by mutableStateOf(false)
            private set

        internal fun trigger(){
            isTriggered = true
        }

        override fun assign(variables: MutableMap<String, Any>) {
            variables[name] = Event(name)
        }
    }
}