package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Stable
internal sealed interface SMGuard {

    public val inputName : String

    public fun check(variables : Map<String, Any>) : Boolean = false

    @Serializable
    @SerialName("Numeric")
    public class Numeric(
        override val inputName : String,
        public val conditionType: SMGuardCondition,
        public val compareTo : String
    ) : SMGuard {

        override fun check(variables: Map<String, Any>): Boolean =
            compare<Float>(variables, inputName, compareTo, conditionType)
    }

    @Serializable
    @SerialName("String")
    public class Str(
        override val inputName : String,
        public val conditionType: SMGuardCondition,
        public val compareTo : String
    ) : SMGuard {

        override fun check(variables: Map<String, Any>): Boolean =
            compare<String>(variables, inputName, compareTo, conditionType)
    }

    @Serializable
    @SerialName("Boolean")
    public class Bool(
        override val inputName : String,
        public val conditionType: SMGuardCondition,
        public val compareTo : String
    ) : SMGuard {

        override fun check(variables: Map<String, Any>): Boolean =
            compare<Boolean>(variables, inputName, compareTo, conditionType)
    }

    @Serializable
    @SerialName("Event")
    public class Event(
        override val inputName : String,
    ) : SMGuard {
        override fun check(variables: Map<String, Any>): Boolean {
            val e = variables[inputName] as? SMInput.Event ?: return false
            return e.isTriggered
        }
    }
}

private inline fun <reified T> compare(
    variables: Map<String, Any>,
    inputName : String,
    compareTo : String,
    condition: SMGuardCondition
) : Boolean{
    val v = variables[inputName] as? T ?: return false
    val compare = if (compareTo.startsWith("#")){
        variables[compareTo.drop(1)] as? T ?: return false
    } else {
        compareTo.toFloatOrNull() ?: return false
    }

    return condition.check(v, compare)
}