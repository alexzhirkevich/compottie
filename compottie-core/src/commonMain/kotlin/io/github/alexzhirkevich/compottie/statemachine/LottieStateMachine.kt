package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import io.github.alexzhirkevich.compottie.LottieAnimatable
import io.github.alexzhirkevich.compottie.LottiePainter
import io.github.alexzhirkevich.compottie.internal.EmptyDrawScope
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.applyParentsMatrix
import io.github.alexzhirkevich.compottie.internal.utils.fastReset
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
public class LottieStateMachine internal constructor(
    internal val initial : String = "",
    internal val states: List<SMState> = emptyList(),
    internal val interactions : List<SMInteraction> = emptyList(),
    internal val inputs : List<SMInput> = emptyList()
) {

    internal val statesMap = states.associateBy { it.name }

    internal fun assignVariables(variables : MutableMap<String, Any>){
        inputs.fastForEach {
            it.assign(variables)
        }
    }

    public companion object {
        public val serializersModule: SerializersModule = SerializersModule {

            polymorphic(SMState::class) {
                subclass(SMState.GlobalState::class)
                subclass(SMState.PlaybackState::class)
            }

            polymorphic(SMAction::class) {
                subclass(SMAction.Url::class)
                subclass(SMAction.Theme::class)
                subclass(SMAction.Increment::class)
                subclass(SMAction.Decrement::class)
                subclass(SMAction.Toggle::class)
                subclass(SMAction.SetBoolean::class)
                subclass(SMAction.SetNumeric::class)
                subclass(SMAction.SetString::class)
                subclass(SMAction.Fire::class)
                subclass(SMAction.Reset::class)
                subclass(SMAction.SetFrame::class)
                subclass(SMAction.SetProgress::class)
                subclass(SMAction.FireCustomEvent::class)
            }

            polymorphic(SMGuard::class) {
                subclass(SMGuard.Bool::class)
                subclass(SMGuard.Numeric::class)
                subclass(SMGuard.Str::class)
                subclass(SMGuard.Event::class)
            }

            polymorphic(SMInput::class) {
                subclass(SMInput.Bool::class)
                subclass(SMInput.Numeric::class)
                subclass(SMInput.Str::class)
                subclass(SMInput.Event::class)
            }

            polymorphic(SMInteraction::class) {
                subclass(SMInteraction.PointerUp::class)
                subclass(SMInteraction.PointerDown::class)
                subclass(SMInteraction.PointerMove::class)
                subclass(SMInteraction.PointerEnter::class)
                subclass(SMInteraction.PointerExit::class)
                subclass(SMInteraction.Click::class)
                subclass(SMInteraction.OnComplete::class)
                subclass(SMInteraction.OnLoopComplete::class)
            }

            polymorphic(SMInput::class) {
                subclass(SMInput.Str::class)
                subclass(SMInput.Numeric::class)
                subclass(SMInput.Bool::class)
                subclass(SMInput.Event::class)
            }

            polymorphic(SMTransition::class) {
                subclass(SMTransition.Default::class)
                subclass(SMTransition.Tweened::class)
            }
        }
    }
}


@Composable
internal fun Modifier.stateMachine(
    painter: LottiePainter,
    stateMachine : String?,
    progress : LottieAnimatable,
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

    val stateMachine by remember(stateMachine, p) {
        derivedStateOf {
            p.animationState.composition.stateMachines?.get(stateMachine)
        }
    }

    val sm = stateMachine ?: return this

    val stateVariables = remember {
        mutableStateMapOf<String, Any>().apply {
            sm.assignVariables(this)
        }
    }



    if (sm.initial !in sm.statesMap)
        return this // no initial state

    var currentState by remember(p, sm) {
        mutableStateOf(checkNotNull(sm.statesMap[sm.initial]))
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(sm,p) {
        launch {
            snapshotFlow {
                currentState
            }.collectLatest {
                it.play(p.composition, progress)
            }
        }

        snapshotFlow {
            currentState.transitions.fastFirstOrNull {
                it.canMove(stateVariables)
            } ?: sm.states.firstNotNullOfOrNull {

                if (it !is SMState.GlobalState)
                    return@firstNotNullOfOrNull null

                it.sortedTransitions
                    .fastFirstOrNull { it.canMove(stateVariables) }
            }
        }.filterNotNull().collectLatest {
            val state = sm.statesMap[it.toState]?.takeIf { it !== currentState }
                ?: return@collectLatest

            try {
                currentState.exitActions.fastForEach {
                    it.invoke(uriHandler, stateVariables, p.animationState, sm)
                }

                state.entryActions.fastForEach {
                    it.invoke(uriHandler, stateVariables, p.animationState, sm)
                }

                state.move(p.animationState, progress, it)
            } finally {
                currentState = state
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

    fun invokeActions(actions: List<SMAction>) {
        actions.fastForEach {
            it.invoke(uriHandler, stateVariables, p.animationState, sm)
        }
    }

    fun getLayersAtPosition(position: Offset): List<Layer> {
        return p.composition.animation.layers.fastFilter {
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
        sm.interactions.filterIsInstance<SMInteraction.PointerEnter>()
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
        Modifier.pointerInput(p, sm) {
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
        Modifier.pointerInput(drawScope, size, painter, contentScale, sm) {

            awaitEachGesture {

                if (
                    sm.interactions.fastAny {
                        it is SMInteraction.PointerDown ||
                                it is SMInteraction.PointerUp ||
                                it is SMInteraction.Click
                    }
                ) {
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
        }
    } else {
        Modifier
    }

    return onSizeChanged { size = it.toSize() }
        .then(hoverModifier)
        .then(tapModifier)
}
