package lottiefiles

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import interactivecontrols.LikeButton
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCancellationBehavior
import io.github.alexzhirkevich.compottie.LottieClipSpec
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.compottie.resetToBeginning
import io.github.alexzhirkevich.shared.generated.resources.Res
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import opacityGrid
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun LottieDetails(
    modifier: Modifier = Modifier,
    onDismiss : () -> Unit,
    onTagClicked : (String) -> Unit,
    file: LottieFile,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.Url(file.lottieSource ?: file.jsonSource ?: "")
    }

    val animatable = rememberLottieAnimatable()

    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var isLooping by rememberSaveable { mutableStateOf(true) }

    var speedIndex by rememberSaveable {
        mutableStateOf(0)
    }

    LaunchedEffect(
        composition,
        isPlaying,
        isLooping,
        speedIndex
    ) {
        if (!isPlaying) return@LaunchedEffect

        animatable.animate(
            composition = composition,
            iterations = if (isLooping) Compottie.IterateForever else 1,
            initialProgress = if (animatable.progress == 1f) 0f else animatable.progress,
            speed = Speed[speedIndex].first,
            continueFromPreviousAnimate = false,
        )
        if (composition != null) {
            isPlaying = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var liked by rememberSaveable { mutableStateOf(false) }
    val url = "https://lottiefiles.com/free-animation/${file.slug}-${file.hash}"

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        BoxWithConstraints {

            val isWideScreen = constraints.maxWidth > LocalDensity.current.run { 500.dp.toPx() }

            Scaffold(
                modifier = Modifier.layout { measurable, constraints ->
                    val w = (constraints.maxWidth * .9).toInt()
                    val shrinkedConstraints = constraints
                        .copy(maxWidth = w, minWidth = w)
                    val placeable = measurable.measure(shrinkedConstraints)
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place((constraints.maxWidth - w) / 2, 0)
                    }
                },
                topBar = {
                    Surface {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            UserAvatar(
                                user = file.user,
                                size = 36.dp
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                if (file.name != null) {
                                    Text(
                                        modifier = Modifier.basicMarquee(),
                                        text = file.name,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                if (file.user.name != null) {
                                    Text(
                                        text = file.user.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            if (isWideScreen) {
                                DownloadButton(
                                    file = file,
                                    compact = false
                                )
                            }

                            if (isWideScreen) {
                                Spacer(Modifier.width(12.dp))
                            }

                            if (isWideScreen) {
                                LikeButton(
                                    liked = liked,
                                    onClick = {
                                        liked = !liked
                                    }
                                )
                            }

                            if (isWideScreen) {
                                Spacer(Modifier.width(12.dp))
                            }

                            if (isWideScreen) {
                                OpenInBrowserButton(url = url)
                            }

                            if (isWideScreen) {
                                Spacer(Modifier.width(12.dp))
                            }

                            FilledTonalIconButton(
                                onClick = onDismiss,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "close"
                                )
                            }
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(it)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isWideScreen) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DownloadButton(
                                file = file,
                                compact = true
                            )

                            LikeButton(
                                liked = liked,
                                onClick = {
                                    liked = !liked
                                }
                            )

                            OpenInBrowserButton(url = url)
                        }
                    }

                    var useOpacityGrid by remember {
                        mutableStateOf(false)
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    ) {
                        val bgColor = file.bgColor?.let(::parseColorValue) ?: Color.White

                        Box(
                            modifier = if (useOpacityGrid)
                                Modifier.opacityGrid(42.dp)
                            else Modifier.background(bgColor)
                        ) {

                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = rememberLottiePainter(
                                    composition = composition,
                                    progress = animatable::value
                                ),
                                contentDescription = file.name
                            )

                            IconButton(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .align(Alignment.BottomStart),
                                onClick = {
                                    useOpacityGrid = !useOpacityGrid
                                }
                            ) {
                                AnimatedContent(
                                    targetState = useOpacityGrid
                                ) { grid ->
                                    Spacer(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .alpha(.75f)
                                            .border(1.dp, Color.Black, CircleShape)
                                            .let {
                                                if (grid) {
                                                    it.background(bgColor)
                                                } else {
                                                    it.opacityGrid(14.dp)
                                                }
                                            }
                                    )
                                }
                            }

                        }
                    }

                    if (isWideScreen) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayButton(
                                isPlaying = isPlaying,
                                onPlaying = { isPlaying = it }
                            )
                            Slider(
                                modifier = Modifier.weight(1f),
                                value = animatable.value,
                                onValueChange = {
                                    coroutineScope.launch {
                                        animatable.snapTo(progress = it)
                                    }
                                },
                            )

                            composition?.let {
                                Text(
                                    "${(it.durationFrames * animatable.value).toInt()} / ${it.durationFrames.toInt()}"
                                )
                            }

                            RepeatButton(
                                isLooping = isLooping,
                                onClick = {
                                    isLooping = !isLooping
                                    isPlaying = true
                                }
                            )

                            SpeedButton(
                                speedIndex = speedIndex,
                                onSpeedIndexChange = {
                                    speedIndex = it
                                }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(top = 32.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Slider(
                                modifier = Modifier.weight(1f),
                                value = animatable.value,
                                onValueChange = {
                                    coroutineScope.launch {
                                        animatable.snapTo(progress = it)
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                RepeatButton(
                                    isLooping = isLooping,
                                    onClick = {
                                        isLooping = !isLooping
                                        isPlaying = true
                                    }
                                )

                                PlayButton(
                                    isPlaying = isPlaying,
                                    onPlaying = { isPlaying = it }
                                )

                                SpeedButton(
                                    speedIndex = speedIndex,
                                    onSpeedIndexChange = {
                                        speedIndex = it
                                    }
                                )
                            }
                        }
                    }


                    Text(
                        text = "Tags",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    ProvideTextStyle(
                        MaterialTheme.typography.labelMedium.let {
                            it.copy(
                                lineHeight = it.fontSize,
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        }
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            file.tags.fastForEach {
                                SuggestionChip(
                                    onClick = {
                                        onTagClicked(it)
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    label = {
                                        Text(
                                            text = it,
                                            maxLines = 1,
                                            lineHeight = LocalTextStyle.current.fontSize,
                                        )
                                    }
                                )
//                            Text(
//                                text = it,
//                                modifier = Modifier
//                                    .clip(CircleShape)
//                                    .background(MaterialTheme.colorScheme.secondaryContainer)
//                                    .padding(
//                                        vertical = 8.dp,
//                                        horizontal = 16.dp
//                                    )
//                            )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val Speed = listOf(
    1f to "1x",
    1.5f to "1.5x",
    2f to "2x",
    .25f to ".25x",
    .5f to ".5x",
    .75f to ".75x",
)

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun DownloadButton(
    modifier: Modifier = Modifier,
    file: LottieFile,
    compact : Boolean
) {
    val uriHandler = LocalUriHandler.current

    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            archive = Res.readBytes("files/dotlottie/download.lottie")
        )
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val progress = animateLottieCompositionAsState(
        composition = composition,
        restartOnPlay = true,
        isPlaying = hovered || focused,
        cancellationBehavior = LottieCancellationBehavior.OnIterationFinish
    )

    val painter = rememberLottiePainter(
        composition = composition,
        progress = progress::value
    )

    if (compact){
        FilledIconButton(
            modifier = modifier,
            interactionSource = interactionSource,
            onClick = {
                (file.lottieSource ?: file.jsonSource)?.let {
                    uriHandler.openUri(it)
                }
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painter,
                contentDescription = null
            )
        }
    } else {
        Button(
            modifier = modifier,
            interactionSource = interactionSource,
            onClick = {
                (file.lottieSource ?: file.jsonSource)?.let {
                    uriHandler.openUri(it)
                }
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painter,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Download",
                maxLines = 1,
                lineHeight = LocalTextStyle.current.fontSize,
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun OpenInBrowserButton(
    modifier: Modifier = Modifier,
    url : String
) {
    val uriHandler = LocalUriHandler.current

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/view_arrow.json").decodeToString()
        )
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val progress = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = hovered || focused,
    )

    val painter = rememberLottiePainter(
        composition = composition,
        progress = { if (progress.isPlaying) progress.value else .2f}
    )

    IconButton(
        modifier = modifier,
        onClick = {
            uriHandler.openUri(url)
        },
        interactionSource = interactionSource
    ){
        Icon(
            modifier = Modifier.size(36.dp),
            painter = painter,
            contentDescription = "Open in browser"
        )
    }
}

@Composable
private fun RepeatButton(
    modifier: Modifier = Modifier,
    isLooping : Boolean,
    onClick : () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        AnimatedContent(isLooping) {
            Icon(
                imageVector = if (it)
                    Icons.Default.Repeat
                else Icons.Default.RepeatOne,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun SpeedButton(
    modifier: Modifier = Modifier,
    speedIndex : Int,
    onSpeedIndexChange : (Int) -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = {
            onSpeedIndexChange(
                if (speedIndex == Speed.lastIndex) {
                    0
                } else {
                    speedIndex + 1
                }
            )
        }
    ) {
        AnimatedContent(speedIndex) {
            Text(
                text = Speed[it].second,
                fontWeight = FontWeight.Bold,
                lineHeight = LocalTextStyle.current.fontSize,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PlayButton(
    modifier: Modifier = Modifier,
    isPlaying : Boolean,
    onPlaying : (Boolean) -> Unit
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            archive = Res.readBytes("files/dotlottie/play_pause.lottie")
        )
    }

    val animatable = remember { Animatable(if (isPlaying) 1f else 0f) }

    val painter = rememberLottiePainter(
        composition = composition,
        progress = animatable::value
    )

    LaunchedEffect(isPlaying){
        animatable.animateTo(
            if (isPlaying) 1f else 0f,
            tween(300, easing = LinearEasing)
        )
    }

    FilledTonalIconButton(
        modifier = modifier,
        onClick = {
            onPlaying(!isPlaying)
        }
    ) {
        Icon(
            painter = painter,
            modifier = Modifier.size(28.dp),
            contentDescription = if (isPlaying) "Pause" else "Play"
        )
    }
}