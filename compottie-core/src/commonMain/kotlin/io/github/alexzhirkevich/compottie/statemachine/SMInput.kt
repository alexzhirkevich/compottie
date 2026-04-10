package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.alexzhirkevich.compottie.LottieStateMachine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SMInput {

    public val name : String

    public fun assign(stateMachine: LottieStateMachine)

    @Serializable
    @SerialName("Numeric")
    public class Numeric(
        override val name : String,
        public val value : Float
    ) : SMInput {

        override fun assign(stateMachine: LottieStateMachine) {
            stateMachine.setFloat(name, value)
        }
    }

    @Serializable
    @SerialName("String")
    public class Str(
        override val name : String,
        public val value : String
    ) : SMInput {

        override fun assign(stateMachine: LottieStateMachine) {
            stateMachine.setString(name, value)
        }
    }

    @Serializable
    @SerialName("Boolean")
    public class Bool(
        override val name : String,
        public val value : Boolean
    ) : SMInput {

        override fun assign(stateMachine: LottieStateMachine) {
            stateMachine.setBoolean(name, value)
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

        override fun assign(stateMachine: LottieStateMachine) {
            if (stateMachine.isFired(name)){
                stateMachine.clearFiredEvents()
            }
        }
    }
}