package io.github.alexzhirkevich.compottie

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.times
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapNotNull
import io.github.alexzhirkevich.compottie.internal.EmptyDrawScope
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.applyParentsMatrix
import io.github.alexzhirkevich.compottie.internal.utils.fastReset
import io.github.alexzhirkevich.compottie.statemachine.SMAction
import io.github.alexzhirkevich.compottie.statemachine.SMConfig
import io.github.alexzhirkevich.compottie.statemachine.SMInteraction
import io.github.alexzhirkevich.compottie.statemachine.SMState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Stable
public sealed interface LottieStateMachine {

    /**
     * Animation state that controls the [LottiePainter] progress
     */
    public val progress : LottieAnimationState

    /**
     * Current machine state. Is null before initialization
     * */
    public val currentState : String?

    /**
     * Events fired by interactions and manually fired with [fire]
     * */
    public val events : Flow<String>

    /**
     * Immediately move to the requested [state]
     */
    public fun snapToState(state : String) : Boolean

    /**
     * Set [Boolean] input property
     */
    public fun setBoolean(key : String, value : Boolean)

    /**
     * Set [String] input property
     */
    public fun setString(key : String, value : String)

    /**
     * Set [Float] input property
     */
    public fun setFloat(key : String, value : Float)

    /**
     * Get [Boolean] input property.
     *
     * The call is stable (value updates the composition, can be used in [snapshotFlow]
     * and [derivedStateOf])
     */
    public fun getBoolean(key : String) : Boolean?

    /**
     * Get [String] input property
     *
     * The call is stable (value updates the composition, can be used in [snapshotFlow]
     * and [derivedStateOf])
     * */
    public fun getString(key : String) : String?

    /**
     * Get [Float] input property
     *
     * The call is stable (value updates the composition, can be used in [snapshotFlow]
     * and [derivedStateOf])
     * */
    public fun getFloat(key : String) : Float?

    /**
     * Fire [event]
     */
    public fun fire(event : String)

    /**
     * Check if [event] was fired and wasn't consumed
     */
    public fun isFired(event : String): Boolean

    /**
     * Consume all fired events
     */
    public fun clearFiredEvents() {}

    /**
     * Reset the state machine input to its initial state
     */
    public fun resetInput(name : String)

    /**
     * Reset the state machine to the initial state
     */
    public fun reset()
}

/**
 * @param progress animation state that controls the [LottiePainter] progress. Usually created with
 * [animateLottieCompositionAsState].
 * If the progress should be controlled by the state machine, just create a [LottieAnimatable] with
 * [rememberLottieAnimatable] and use it for both [rememberLottiePainter] progress and [Lottie] progress
 * */
@Composable
public fun rememberLottieStateMachine(
    id : String,
    composition: LottieComposition?,
    progress: LottieAnimationState
): LottieStateMachine? = retain(id, composition, progress) {
    composition?.let {
        LottieStateMachineImpl(
            config = composition.stateMachines?.get(id),
            progress = progress
        )
    }
}



internal class LottieStateMachineImpl(
    internal val config : SMConfig?,
    override val progress: LottieAnimationState
) : LottieStateMachine {

    private val inputs = mutableStateMapOf<String, Any>()

    private val _events = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    override val events: Flow<String> = _events.asSharedFlow()

    override var currentState: String? by mutableStateOf(config?.initial)
        private set

    private var triggeredEvent by mutableStateOf<String?>(null)

    init {
        reset()
    }

    override fun snapToState(state: String): Boolean {
        return if (config?.statesMap?.contains(state) == true) {
            currentState = state
            true
        } else {
            false
        }
    }

    override fun setBoolean(key: String, value: Boolean) {
        inputs[key] = value
    }

    override fun setString(key: String, value: String) {
        inputs[key] = value
    }

    override fun setFloat(key: String, value: Float) {
        inputs[key] = value
    }

    override fun getBoolean(key: String): Boolean? {
        return inputs[key] as? Boolean
    }

    override fun getString(key: String): String? {
        return inputs[key] as? String
    }

    override fun getFloat(key: String): Float? {
        return inputs[key] as? Float
    }

    override fun fire(event: String)  {
        triggeredEvent = event
    }

    override fun isFired(event: String): Boolean {
        return triggeredEvent == event
    }

    override fun clearFiredEvents() {
        triggeredEvent = null
    }

    override fun resetInput(name: String) {
        config?.inputsMap?.get(name)?.assign(this)
    }

    override fun reset() {
        clearFiredEvents()
        inputs.clear()
        config?.assignVariables(this)
    }
}

internal const val SMRefPrefix = "$"

internal fun LottieStateMachine.floatOrValue(nameOrValue : String) : Float? {
    return if (nameOrValue.startsWith(SMRefPrefix))
        getFloat(nameOrValue.drop(1))
    else
        nameOrValue.toFloatOrNull()
}

internal fun LottieStateMachine.booleanOrValue(nameOrValue : String) : Boolean? {
    return if (nameOrValue.startsWith(SMRefPrefix))
        getBoolean(nameOrValue.drop(1))
    else
        nameOrValue.toBooleanStrictOrNull()
}

internal fun LottieStateMachine.stringOrValue(nameOrValue : String) : String? {
    return if (nameOrValue.startsWith(SMRefPrefix))
        getString(nameOrValue.drop(1))
    else
        nameOrValue
}

@Composable
internal fun Modifier.stateMachine(
    painter: LottiePainter,
    stateMachine : LottieStateMachine?,
    contentScale: ContentScale,
    alignment: Alignment
) : Modifier {

    if (stateMachine == null)
        return this

    val painter by remember(painter) {
        derivedStateOf {
            painter.painter()
        }
    }

    val p = painter ?: return this

    val smConfig by remember(stateMachine) {
        derivedStateOf {
            when (stateMachine){
                is LottieStateMachineImpl -> stateMachine.config
            }
        }
    }

    val progress : LottieAnimatable by remember(stateMachine) {
        derivedStateOf {
            when (val p = stateMachine.progress) {
                is LottieAnimatable -> p
            }
        }
    }

    val sm = smConfig ?: return this

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(sm,p, progress) {
        launch {
            snapshotFlow {
                stateMachine.currentState?.let(sm.statesMap::get)
            }.filterNotNull().collectLatest {
                it.play(p.composition, progress)
            }
        }

        snapshotFlow {
            stateMachine.currentState
                ?.let(sm.statesMap::get)
                ?.transitions
                ?.fastFirstOrNull { it.canMove(stateMachine) }
                ?: sm.states.firstNotNullOfOrNull {

                    if (it !is SMState.GlobalState)
                        return@firstNotNullOfOrNull null

                    it.sortedTransitions
                        .fastFirstOrNull { it.canMove(stateMachine) }
                }
        }.filterNotNull().collectLatest { transition ->

            val state = sm.statesMap[transition.toState]
                ?.takeIf { it.name !== stateMachine.currentState }
                ?: return@collectLatest

            val currentState = sm.statesMap[stateMachine.currentState]
                ?: return@collectLatest

            if (currentState is SMState.PlaybackState && currentState.final)
                return@collectLatest

            try {
                currentState.exitActions.fastForEach {
                    it.invoke(uriHandler, stateMachine, p.animationState, progress)
                }

                state.entryActions.fastForEach {
                    it.invoke(uriHandler, stateMachine, p.animationState, progress)
                }

                stateMachine.clearFiredEvents()

                state.move(p.animationState, progress, transition)
            } finally {
                stateMachine.snapToState(state.name)
            }
        }
    }

    if (sm.interactions.isEmpty())
        return this

    var size by remember {
        mutableStateOf(Size.Zero)
    }

    val drawScope = remember(density, layoutDirection) {
        EmptyDrawScope(density, layoutDirection)
    }

    val scale = remember(contentScale, p.intrinsicSize , size) {
        contentScale.computeScaleFactor(p.intrinsicSize, size)
    }

    val translate = remember(alignment, p.intrinsicSize, scale, size, layoutDirection) {
        alignment.align(
            size = (p.intrinsicSize * scale).toIntSize(),
            space = size.toIntSize(),
            layoutDirection = layoutDirection
        ).toOffset()
    }

    val matrix = remember { Matrix() }
    val bounds = remember { MutableRect(0f, 0f, 0f, 0f) }

    fun mapOffset(o: Offset): Offset {
        return (o - translate).let {
            Offset(it.x / scale.scaleX, it.y / scale.scaleY)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    fun invokeActions(actions: List<SMAction>) {
        coroutineScope.launch {
            actions.fastForEach {
                it.invoke(uriHandler, stateMachine, p.animationState, progress)
            }
        }
    }

    val listenedLayers = remember(smConfig, p.composition) {
        val layers = smConfig?.interactions
            ?.fastMapNotNull { it.layerName }
            .orEmpty()

        p.composition.animation.layers.filter { it.name in layers }
    }

    fun getLayersAtPosition(position: Offset): List<Layer> {
        return listenedLayers.fastFilter {
            matrix.fastReset()
            bounds.set(0f, 0f, 0f, 0f)
            it.applyParentsMatrix(matrix, p.animationState)
            it.getBounds(
                drawScope = drawScope,
                parentMatrix = matrix,
                applyParents = false,
                state = p.animationState,
                outBounds = bounds
            )

            bounds.contains(position)
        }
    }

    var lastHoveredLayers by remember {
        mutableStateOf(emptyList<Layer>())
    }

    val onEnter = remember(sm) {
        sm.interactions.filterIsInstance<SMInteraction.PointerEnter>()
    }
    val onEnterAnim = remember(onEnter) {
        onEnter.fastFilter { it.layerName == null }
    }

    val onExit = remember(sm) {
        sm.interactions.filterIsInstance<SMInteraction.PointerExit>()
    }

    val onExitAnim = remember(onExit) {
        onExit.fastFilter { it.layerName == null }
    }

    val hasLayerHoverInteractions = remember(sm) {
        onExit.fastAny { it.layerName != null }
                || onEnter.fastAny { it.layerName != null }
    }

    val hoverModifier = if (
        onEnter.isNotEmpty() || onExit.isNotEmpty()
    ) {
        Modifier.pointerInput(p, sm, progress) {
            awaitEachGesture {
                val p = awaitPointerEvent()

                when (p.type) {

                    PointerEventType.Enter -> onEnterAnim.fastForEach {
                        invokeActions(it.actions)
                    }

                    PointerEventType.Exit -> onExitAnim.fastForEach {
                        invokeActions(it.actions)
                    }

                    PointerEventType.Move -> {

                        if (!hasLayerHoverInteractions)
                            return@awaitEachGesture

                        val hoveredLayers = getLayersAtPosition(
                            mapOffset(p.changes.first().position)
                        )

                        val enterLayers = if (onEnter.isNotEmpty())
                            hoveredLayers - lastHoveredLayers
                        else emptyList()

                        val exitLayers = if (onExit.isNotEmpty())
                            lastHoveredLayers - hoveredLayers
                        else emptyList()

                        lastHoveredLayers = hoveredLayers

                        onEnter.fastForEach {
                            if (
                                it.layerName != null
                                && enterLayers.fastAny { l -> l.name == it.layerName }
                            ) {
                                invokeActions(it.actions)
                            }
                        }

                        onExit.fastForEach {
                            if (
                                it.layerName != null
                                && exitLayers.fastAny { l -> l.name == it.layerName }
                            ) {
                                invokeActions(it.actions)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Modifier
    }

    val tapModifier = if (
        sm.interactions.fastAny {
            it is SMInteraction.PointerDown ||
                    it is SMInteraction.PointerUp ||
                    it is SMInteraction.Click
        }
    ) {
        Modifier.pointerInput(drawScope, size, painter, contentScale, sm, progress) {

            awaitEachGesture {

                val down = awaitFirstDown()
                val downLayers = getLayersAtPosition(mapOffset(down.position))

                sm.interactions.fastForEach {
                    if (
                        it is SMInteraction.PointerDown &&
                        (it.layerName == null || downLayers.fastAny { l -> l.name == it.layerName })
                    ) {
                        invokeActions(it.actions)
                    }
                }

                val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                val upLayers = getLayersAtPosition(mapOffset(up.position))

                sm.interactions.fastForEach {

                    val invokeUp = it is SMInteraction.PointerUp &&
                            (it.layerName == null || upLayers.fastAny { l -> l.name == it.layerName })

                    val invokeClick = it is SMInteraction.Click &&
                            (it.layerName == null ||
                                    downLayers.fastAny { l -> l.name == it.layerName } &&
                                    upLayers.fastAny { l -> l.name == it.layerName })

                    if (invokeUp || invokeClick) {
                        invokeActions(it.actions)
                    }
                }
            }
        }
    } else {
        Modifier
    }

    val state by remember(stateMachine, sm) {
        derivedStateOf {
            sm.statesMap[stateMachine.currentState]
        }
    }

    return onSizeChanged { size = it.toSize() }
        .then(hoverModifier)
        .then(tapModifier)
        .then(state?.modifier ?: Modifier)
}

