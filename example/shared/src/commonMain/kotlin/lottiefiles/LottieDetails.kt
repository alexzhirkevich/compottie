package lottiefiles

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import opacityGrid

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
        try {
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
        } catch (t : CancellationException){
            isPlaying = false
            throw t
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        BoxWithConstraints {

            val isWideScreen = constraints.maxWidth > LocalDensity.current.run { 500.dp.toPx() }

            Column(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val w = (constraints.maxWidth * .9).toInt()
                        val shrinkedConstraints = constraints
                            .copy(maxWidth = w, minWidth = w)
                        val placeable = measurable.measure(shrinkedConstraints)
                        layout(constraints.maxWidth, placeable.height) {
                            placeable.place((constraints.maxWidth - w) / 2, 0)
                        }
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
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

                    DownloadButton(
                        file = file,
                        compact = !isWideScreen
                    )

                    if (isWideScreen){
                        Spacer(Modifier.width(12.dp))
                    }

                    val uriHandler = LocalUriHandler.current

                    IconButton(
                        onClick = {
                            uriHandler.openUri("https://lottiefiles.com/free-animation/${file.slug}-${file.hash}")
                        }
                    ){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "View animation on LottieFiles"
                        )
                    }

                    if (isWideScreen){
                        Spacer(Modifier.width(12.dp))
                    }

                    IconButton(
                        onClick = onDismiss,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close"
                        )
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
                        ){
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
                                    Text(it)
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

private val Speed = listOf(
    1f to "1x",
    1.5f to "1.5x",
    2f to "2x",
    .25f to ".25x",
    .5f to ".5x",
    .75f to ".75x",
)

@Composable
private fun DownloadButton(
    modifier: Modifier = Modifier,
    file: LottieFile,
    compact : Boolean
) {
    val uriHandler = LocalUriHandler.current

    if (compact){
        FilledIconButton(
            modifier = modifier,
            onClick = {
                (file.lottieSource ?: file.jsonSource)?.let {
                    uriHandler.openUri(it)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = null
            )
        }
    } else {
        Button(
            modifier = modifier,
            onClick = {
                (file.lottieSource ?: file.jsonSource)?.let {
                    uriHandler.openUri(it)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = null
            )
            Text("Download")
        }
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
                lineHeight = LocalTextStyle.current.fontSize
            )
        }
    }
}

@Composable
private fun PlayButton(
    modifier: Modifier = Modifier,
    isPlaying : Boolean,
    onPlaying : (Boolean) -> Unit
) {

    FilledTonalIconButton(
        modifier = modifier,
        onClick = { onPlaying(!isPlaying) }
    ) {
        AnimatedContent(isPlaying) {
            Icon(
                imageVector = if (it)
                    Icons.Default.Pause
                else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}