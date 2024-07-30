package interactivecontrols

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val CellSize = 150.dp
private val darkColors = darkColorScheme()
private val lightColors = lightColorScheme()

@Composable
public fun InteractiveControlsScreen() {

    var isDay by remember {
        mutableStateOf(true)
    }

    var liked by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        color = animateColorAsState(
            if (isDay) lightColors.background else darkColors.background
        ).value
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(CellSize)
        ) {
            item {
                Box(
                    modifier = Modifier.size(CellSize),
                    contentAlignment = Alignment.Center
                ) {
                    DayNightSwitch(
                        isDay = isDay,
                        modifier = Modifier
                            .height(42.dp)
                            .width(78.dp)
                            .clip(CircleShape)
                            .clickable {
                                isDay = !isDay
                            }
                            .padding(4.dp)
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier.size(CellSize),
                    contentAlignment = Alignment.Center
                ) {
                    LikeButton(
                        liked = liked,
                        onClick = { liked = !liked }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun DayNightSwitch(
    modifier: Modifier = Modifier,
    isDay : Boolean,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            archive = Res.readBytes("files/dotlottie/day_night_switch.lottie")
        )
    }

    val animatable = remember { Animatable(initialValue = if (isDay) 0f else 1f) }

    LaunchedEffect(isDay){
        animatable.animateTo(
            targetValue = if (isDay) 0f else 1f,
            animationSpec = tween(
                composition?.duration?.inWholeMilliseconds?.toInt() ?: 0,
                easing = LinearEasing
            )
        )
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = animatable::value
        ),
        modifier = modifier,
        contentDescription = "Day-night switch"
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun LikeButton(
    modifier: Modifier = Modifier,
    liked : Boolean,
    onClick : () -> Unit,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            archive = Res.readBytes("files/dotlottie/like.lottie")
        )
    }

    val animatable = rememberLottieAnimatable()
    val interactionSource = remember { MutableInteractionSource() }

    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
       when {
           pressed -> 1.75f
           hovered || focused -> 2.25f
           else -> 2f
       }
    )

    LaunchedEffect(liked) {
        if (liked) {
            animatable.animate(composition)
        }
        animatable.snapTo(composition, 0f)
    }


    AnimatedContent(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        targetState = liked,
        transitionSpec = {
            scaleIn() togetherWith scaleOut()
        }
    ){
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = animatable::value,
                clipToCompositionBounds = false
            ),
            modifier = Modifier.size(42.dp).graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            colorFilter = if (liked || animatable.value != 0f) null else ColorFilter.tint(Color.LightGray),
            contentDescription = "Like",
        )
    }
}