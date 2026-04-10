package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.LottieStateMachine
import io.github.alexzhirkevich.compottie.booleanOrValue
import io.github.alexzhirkevich.compottie.floatOrValue
import io.github.alexzhirkevich.compottie.stringOrValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Stable
internal sealed interface SMGuard {

    public val inputName : String

    public fun check(machine: LottieStateMachine) : Boolean = false

    @Serializable
    @SerialName("Numeric")
    public class Numeric(
        override val inputName : String,
        public val conditionType: SMGuardCondition,
        public val compareTo : String
    ) : SMGuard {

        override fun check(machine: LottieStateMachine): Boolean {
            return conditionType.check(
                machine.getFloat(inputName) ?: return false,
                machine.floatOrValue(compareTo) ?: return false
            )
        }
    }

    @Serializable
    @SerialName("String")
    public class Str(
        override val inputName : String,
        public val conditionType: SMGuardCondition,
        public val compareTo : String
    ) : SMGuard {

        override fun check(machine: LottieStateMachine): Boolean {
            return conditionType.check(
                machine.getString(inputName) ?: return false,
                machine.stringOrValue(compareTo) ?: return false
            )
        }
    }

    @Serializable
    @SerialName("Boolean")
    public class Bool(
        override val inputName : String,
        public val conditionType: SMGuardCondition,
        public val compareTo : String
    ) : SMGuard {

        override fun check(machine: LottieStateMachine): Boolean {
            return conditionType.check(
                machine.getBoolean(inputName) ?: return false,
                machine.booleanOrValue(compareTo) ?: return false
            )
        }
    }

    @Serializable
    @SerialName("Event")
    public class Event(
        override val inputName : String,
    ) : SMGuard {

        override fun check(machine: LottieStateMachine): Boolean {
            return machine.isFired(inputName)
        }
    }
}
