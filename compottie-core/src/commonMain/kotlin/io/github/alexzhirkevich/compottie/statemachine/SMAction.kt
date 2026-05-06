package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.ui.platform.UriHandler
import io.github.alexzhirkevich.compottie.LottieStateMachine
import io.github.alexzhirkevich.compottie.floatOrValue
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SMAction {

    public suspend operator fun invoke(
        uriHandler: UriHandler,
        stateMachine: LottieStateMachine,
        state: AnimationState
    )

    @Serializable
    @SerialName("Url")
    public class Url(
        public val url : String,
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            uriHandler.openUri(url)
        }
    }

    @Serializable
    @SerialName("Theme")
    public class Theme(
        public val value : String,
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            state.theme = value
        }
    }

    @Serializable
    @SerialName("Increment")
    public class Increment(
        public val inputName : String,
        public val value : String
    ) : SMAction{

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            increment(stateMachine, inputName, value)
        }
    }

    @Serializable
    @SerialName("Decrement")
    public class Decrement(
        public val inputName : String,
        public val value : String
    ) : SMAction {
        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            increment(stateMachine, inputName, value, -1)
        }
    }

    @Serializable
    @SerialName("Toggle")
    public class Toggle(
        public val inputName : String,
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            val b = stateMachine.getBoolean(inputName) ?: return
            stateMachine.setBoolean(inputName, b.not())
        }
    }

    @Serializable
    @SerialName("SetBoolean")
    public class SetBoolean(
        public val inputName : String,
        public val value : Boolean
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            stateMachine.setBoolean(inputName, value)
        }
    }

    @Serializable
    @SerialName("SetString")
    public class SetString(
        public val inputName : String,
        public val value : String
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            stateMachine.setString(inputName, value)
        }
    }

    @Serializable
    @SerialName("SetNumeric")
    public class SetNumeric(
        public val inputName : String,
        public val value : Float
    ) : SMAction {
        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            stateMachine.setFloat(inputName, value)
        }
    }

    @Serializable
    @SerialName("Fire")
    public class Fire(
        public val inputName : String,
    ) : SMAction {
        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            stateMachine.fire(inputName)
        }
    }

    @Serializable
    @SerialName("Reset")
    public class Reset(
        public val inputName : String,
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState,
        ) {
            stateMachine.resetInput(inputName)
        }
    }

    @Serializable
    @SerialName("SetFrame")
    public class SetFrame(
        public val value : String
    ) : SMAction {
        
        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {

            val frame = stateMachine.floatOrValue(value) ?: return

            stateMachine.animatable.snapTo(
                composition = state.composition,
                progress = state.composition.frameToProgress(frame)
            )
        }
    }

    @Serializable
    @SerialName("SetProgress")
    public class SetProgress(
        public val inputName : String,
        public val value : String
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            stateMachine.animatable.snapTo(
                composition = state.composition,
                progress = stateMachine.floatOrValue(value) ?: return
            )
        }
    }

    @Serializable
    @SerialName("FireCustomEvent")
    public class FireCustomEvent(
        public val value : String
    ) : SMAction {

        override suspend fun invoke(
            uriHandler: UriHandler,
            stateMachine: LottieStateMachine,
            state: AnimationState
        ) {
            stateMachine.fire(value)
        }
    }
}

private fun increment(
    stateMachine: LottieStateMachine,
    inputName: String,
    value : String,
    sign : Int = 1
){

    val v = stateMachine.getFloat(inputName) ?: return
    val diff = stateMachine.floatOrValue(value) ?: return

    stateMachine.setFloat(inputName, v + diff * sign)
}