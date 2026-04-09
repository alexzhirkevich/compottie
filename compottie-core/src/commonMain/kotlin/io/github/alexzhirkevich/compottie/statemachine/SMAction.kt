package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.ui.platform.UriHandler
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SMAction {

    public operator fun invoke(
        uriHandler: UriHandler,
        variables: MutableMap<String, Any>,
        state: AnimationState,
        stateMachine: LottieStateMachine
    ){}

    @Serializable
    @SerialName("Url")
    public class Url(
        public val url : String,
    ) : SMAction {
        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            uriHandler.openUri(url)
        }
    }

    @Serializable
    @SerialName("Theme")
    public class Theme(
        public val value : String,
    ) : SMAction {
        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
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

        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            increment(variables, inputName, value)
        }
    }

    @Serializable
    @SerialName("Decrement")
    public class Decrement(
        public val inputName : String,
        public val value : String
    ) : SMAction {
        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            increment( variables, inputName, value, -1)
        }
    }

    @Serializable
    @SerialName("Toggle")
    public class Toggle(
        public val inputName : String,
    ) : SMAction {

        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            val v = variables[inputName] as? Boolean ?: return
            variables[inputName] = !v
        }
    }

    @Serializable
    @SerialName("SetBoolean")
    public class SetBoolean(
        public val inputName : String,
        public val value : Boolean
    ) : SMAction {

        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            variables[inputName] = value
        }
    }

    @Serializable
    @SerialName("SetString")
    public class SetString(
        public val inputName : String,
        public val value : String
    ) : SMAction {
        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            variables[inputName] = value
        }
    }

    @Serializable
    @SerialName("SetNumeric")
    public class SetNumeric(
        public val inputName : String,
        public val value : Float
    ) : SMAction {
        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            variables[inputName] = value
        }
    }

    @Serializable
    @SerialName("Fire")
    public class Fire(
        public val inputName : String,
    ) : SMAction {
        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine
        ) {
            val e = variables[inputName] as? SMInput.Event
            e?.trigger()
        }
    }

    @Serializable
    @SerialName("Reset")
    public class Reset(
        public val inputName : String,
    ) : SMAction {

        override fun invoke(
            uriHandler: UriHandler,
            variables: MutableMap<String, Any>,
            state: AnimationState,
            stateMachine: LottieStateMachine,
        ) {
            variables.clear()
            stateMachine.assignVariables(variables)
        }
    }

    @Serializable
    @SerialName("SetFrame")
    public class SetFrame(
        public val inputName : String,
        public val value : String
    ) : SMAction

    @Serializable
    @SerialName("SetProgress")
    public class SetProgress(
        public val inputName : String,
        public val value : String
    ) : SMAction

    @Serializable
    @SerialName("FireCustomEvent")
    public class FireCustomEvent(
        public val inputName : String,
        public val value : String
    ) : SMAction
}

private fun increment(
    stateVariables: MutableMap<String, Any>,
    inputName: String,
    value : String,
    sign : Int = 1
){

    val v = stateVariables[inputName] as? Float ?: return
    val diff = if (value.startsWith("#")){
        stateVariables[value.drop(1)] as? Float ?: return
    } else {
        value.toFloatOrNull() ?: return
    }

    stateVariables[inputName] = v + diff * sign
}