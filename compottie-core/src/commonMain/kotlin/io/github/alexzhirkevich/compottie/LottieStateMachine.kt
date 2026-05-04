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
import androidx.compose.runtime.rememberUpdatedState
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
import io.github.alexzhirkevich.compottie.internal.utils.fastReset
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import io.github.alexzhirkevich.compottie.internal.utils.preTranslate
import io.github.alexzhirkevich.compottie.statemachine.SMAction
import io.github.alexzhirkevich.compottie.statemachine.SMConfig
import io.github.alexzhirkevich.compottie.statemachine.SMState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


/**
 * State machine controller. Can be used to set/observe inputs, fire and subscribe to events,
 * change/get the current state
 * */
@Stable
public sealed interface LottieStateMachine {

    /**
     * Animation state that controls the [LottiePainter] progress
     */
    public val animatable : LottieAnimatable

    /**
     * Current machine state. Is null before initialization
     * */
    public val currentState : String?

    /**
     * Events fired by the interactions and fired manually with [fire]
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
 * Create and remember [LottieStateMachine]
 *
 * @param id state machine identifier
 * @param composition composition that initialize the [LottiePainter]
 * @param animatable animator that controls the [LottiePainter] progress
 *
 * @see [rememberLottieAnimatable]
 * @see [rememberLottieComposition]
 * */
@Composable
public fun rememberLottieStateMachine(
    id : String,
    composition: LottieComposition?,
    animatable: LottieAnimatable
): LottieStateMachine? {

    return retain(id, composition, animatable) {
        composition?.let {
            LottieStateMachineImpl(
                config = composition.stateMachines?.get(id) ?: return@let null,
                animatable = animatable
            )
        }
    }
}

internal class LottieStateMachineImpl(
    internal val config : SMConfig,
    override val animatable: LottieAnimatable
) : LottieStateMachine {

    private val inputs = mutableStateMapOf<String, Any>()

    private val _events = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    override val events: Flow<String> = _events.asSharedFlow()

    override var currentState: String? by mutableStateOf(config.initial)
        private set

    private var triggeredEvent by mutableStateOf<String?>(null)

    init {
        reset()
    }

    override fun snapToState(state: String): Boolean {
        return if (config.statesMap.contains(state)) {
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

    override fun fire(event: String) {
        triggeredEvent = event
    }

    override fun isFired(event: String): Boolean {
        return triggeredEvent == event
    }

    override fun clearFiredEvents() {
        triggeredEvent = null
    }

    override fun resetInput(name: String) {
        config.inputsMap[name]?.assign(this)
    }

    override fun reset() {
        clearFiredEvents()
        inputs.clear()
        config.assignVariables(this)
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
            when (stateMachine) {
                is LottieStateMachineImpl -> stateMachine.config
            }
        }
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current
    val stateMachine by rememberUpdatedState(stateMachine)

    LaunchedEffect(smConfig, p, stateMachine, stateMachine.animatable) {
        launch {
            snapshotFlow {
                stateMachine.currentState?.let(smConfig.statesMap::get)
            }.filterNotNull().collectLatest {
                it.play(p.composition, stateMachine.animatable)
            }
        }

        snapshotFlow {
            stateMachine.currentState
                ?.let(smConfig.statesMap::get)
                ?.transitions
                ?.fastFirstOrNull { it.canMove(stateMachine) }
                ?: smConfig.globalStates.firstNotNullOfOrNull {
                    it.sortedTransitions.fastFirstOrNull {
                        it.canMove(stateMachine)
                    }
                }
        }.filterNotNull().collectLatest { transition ->

            val state = smConfig.statesMap[transition.toState]
                ?.takeIf { it.name !== stateMachine.currentState }
                ?: return@collectLatest

            val currentState = smConfig.statesMap[stateMachine.currentState]
                ?: return@collectLatest

            if (currentState is SMState.PlaybackState && currentState.final)
                return@collectLatest

            try {
                currentState.exitActions.fastForEach {
                    it.invoke(uriHandler, stateMachine, p.animationState)
                }

                state.entryActions.fastForEach {
                    it.invoke(uriHandler, stateMachine, p.animationState)
                }

                stateMachine.clearFiredEvents()

                state.move(p.animationState, stateMachine.animatable, transition)
            } finally {
                stateMachine.snapToState(state.name)
            }
        }
    }

    val state by remember(stateMachine, smConfig) {
        derivedStateOf {
            smConfig.statesMap[stateMachine.currentState]
        }
    }

    if (smConfig.interactions.isEmpty())
        return then (state?.modifier ?: Modifier)

    var size by remember {
        mutableStateOf(Size.Zero)
    }

    val drawScope = remember(density, layoutDirection) {
        EmptyDrawScope(density, layoutDirection)
    }

    val scale = remember(contentScale, p.intrinsicSize, size) {
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

    val coroutineScope = rememberCoroutineScope()

    val invokeActions by rememberUpdatedState { actions: List<SMAction> ->
        coroutineScope.launch {
            actions.fastForEach {
                it.invoke(uriHandler, stateMachine, p.animationState)
            }
        }
    }

    val listenedLayers = remember(smConfig, p.composition) {
        val layers = smConfig.interactions
            .fastMapNotNull { it.layerName }

        p.composition.animation.layers.filter { it.name in layers }
    }

    LaunchedEffect(matrix, scale,translate){
        matrix.fastReset()
        matrix.preTranslate(translate.x, translate.y)
        matrix.preScale(scale.scaleX, scale.scaleY)
    }

    fun getLayersAtPosition(position: Offset): List<Layer> {
        return listenedLayers.fastFilter {
            bounds.set(0f, 0f, 0f, 0f)
            it.getBounds(
                drawScope = drawScope,
                parentMatrix = matrix,
                applyParents = true,
                state = p.animationState,
                outBounds = bounds
            )

            bounds.contains(position)
        }
    }

    var lastHoveredLayers by remember {
        mutableStateOf(emptyList<Layer>())
    }

    val onEnterAnim = remember(smConfig.enterInteractions) {
        smConfig.enterInteractions.fastFilter { it.layerName == null }
    }

    val onExitAnim = remember(smConfig.exitInteractions) {
        smConfig.exitInteractions.fastFilter { it.layerName == null }
    }

    val hasLayerHoverInteractions = remember(smConfig) {
        onEnterAnim.size != smConfig.enterInteractions.size ||
                onExitAnim.size != smConfig.exitInteractions.size
    }

    val hoverModifier = if (
        smConfig.hasHoverInteractions() && !p.animationState.isTweenRunning
    ) {
        Modifier.pointerInput(p, smConfig, invokeActions) {
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
                            p.changes.firstOrNull()?.position ?: return@awaitEachGesture
                        )

                        val enterLayers = hoveredLayers - lastHoveredLayers
                        val exitLayers = lastHoveredLayers - hoveredLayers

                        lastHoveredLayers = hoveredLayers

                        smConfig.enterInteractions.fastForEach {
                            if (
                                it.layerName != null
                                && enterLayers.fastAny { l -> l.name == it.layerName }
                            ) {
                                invokeActions(it.actions)
                            }
                        }

                        smConfig.exitInteractions.fastForEach {
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
        smConfig.hasPointerInteractions() && !p.animationState.isTweenRunning
    ) {
        Modifier.pointerInput(drawScope, size, painter, contentScale, smConfig, invokeActions) {

            awaitEachGesture {

                val down = awaitFirstDown()
                val downLayers = getLayersAtPosition(down.position)

                smConfig.downInteractions.fastForEach {
                    if (
                        it.layerName == null ||
                        downLayers.fastAny { l -> l.name == it.layerName }
                    ) {
                        invokeActions(it.actions)
                    }
                }

                val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                val upLayers = getLayersAtPosition(up.position)

                smConfig.upInteractions.fastForEach {
                    if (
                        it.layerName == null ||
                        upLayers.fastAny { l -> l.name == it.layerName }
                    ){
                        invokeActions(it.actions)
                    }
                }

                smConfig.clickInteractions.fastForEach {
                    if (
                        it.layerName == null ||
                        downLayers.fastAny { l -> l.name == it.layerName } &&
                        upLayers.fastAny { l -> l.name == it.layerName }
                    ) {
                        invokeActions(it.actions)
                    }
                }
            }
        }
    } else {
        Modifier
    }


    return onSizeChanged { size = it.toSize() }
        .then(hoverModifier)
        .then(tapModifier)
        .then(state?.modifier ?: Modifier)
//        .drawWithContent {
//            listenedLayers.fastForEach {
//                bounds.set(0f, 0f, 0f, 0f)
//                it.getBounds(
//                    drawScope = drawScope,
//                    parentMatrix = matrix,
//                    applyParents = true,
//                    state = p.animationState,
//                    outBounds = bounds
//                )
//                drawRect(
//                    color = Color.Red,
//                    topLeft = bounds.topLeft,
//                    size = bounds.size,
//                    style = Stroke(1.dp.toPx())
//                )
//            }
//            drawContent()
//        }
}

