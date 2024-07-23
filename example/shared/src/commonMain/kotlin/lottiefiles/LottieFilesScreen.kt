package lottiefiles

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
internal fun LottieFilesScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: LottieFilesViewModel = viewModel { LottieFilesViewModel() }
) {
    DisposableEffect(0){
        val l = Compottie.logger
        Compottie.logger = null
        onDispose {
            Compottie.logger = l
        }
    }

    val selectedFile = viewModel.selectedFile.collectAsState().value

    if (selectedFile != null) {
        Dialog(
            onDismissRequest = {
                viewModel.onFileSelected(null)
            }
        ) {
            LottieDetails(
                file = selectedFile,
                onDismiss = {
                    viewModel.onFileSelected(null)
                }
            )
        }
    }

    BoxWithConstraints {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar(
                viewModel = viewModel
            )

            val files = viewModel.files.collectAsState().value

            LazyVerticalGrid(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                columns = GridCells.Adaptive(200.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(
                    items = files,
                    key = LottieFile::id
                ) {
                    LottieCard(
                        file = it,
                        onClick = { viewModel.onFileSelected(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            PageSelector(
                page = viewModel.page.collectAsState().value,
                pageCount = viewModel.pageCount.collectAsState().value,
                onPageSelected = viewModel::onPageSelected,
                modifier =  Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    viewModel: LottieFilesViewModel
) {
    var searchBarActive by remember {
        mutableStateOf(false)
    }

    val query by viewModel.search.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    SearchBar(
        modifier = modifier,
        query = query,
        onQueryChange = viewModel::onSearch,
        onSearch = viewModel::onSearch,
        active = false,//searchBarActive && query.isNotBlank() && suggestions.isNotEmpty(),
        placeholder = {
            Text("Search...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    viewModel.onSearch("")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear"
                )
            }
        },
        onActiveChange = {
            searchBarActive = it
        }
    ) {
        suggestions.forEach {
            Text(
                text = it.query,
                modifier = Modifier.clickable {
                    viewModel.onSearch(it.query)
                }
            )
        }
    }
}

private val PageSelectorSize = 36.dp


@Composable
private fun PageSelector(
    page : Int,
    pageCount : Int,
    onPageSelected : (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val pageSizePx = remember(density) {
        with(density) {
            PageSelectorSize.toPx()
        }
    }


    SubcomposeLayout(modifier) { constraints ->

        val spaceLeft = (constraints.maxWidth / pageSizePx).toInt().coerceAtMost(11)

        val items = buildList {
            addAll((1..pageCount).map { SelectorButton.Page(it) })
            if (page > 1) {
                add(0, SelectorButton.Backward)
            }
            if (page < pageCount){
                add(SelectorButton.Forward)
            }
        }.toMutableList()

        val indexOfFirst =  { if (page > 1) 2 else 1 }
        val indexOfLast = { if (page < pageCount) items.size - 3 else items.size - 2 }

        while (items.size > spaceLeft) {

            val start = if (page == 1){
                0
            } else {
                (items[indexOfFirst()] as SelectorButton.Page).i
            }
            val end = if (page == pageCount){
                0
            } else {
                (items[indexOfLast()] as SelectorButton.Page).i
            }

            if (abs(start-page) > abs(end - page)){
                items.removeAt(indexOfFirst())
            } else {
                items.removeAt(indexOfLast())
            }
        }

        if (items.size == spaceLeft) {

            if (spaceLeft > 7 && (items[indexOfFirst()] as? SelectorButton.Page)?.i != 2) {
                items[indexOfFirst()] = SelectorButton.Dots
            }
            if (spaceLeft > 8 && (items[indexOfLast()] as? SelectorButton.Page)?.i != pageCount - 1) {
                items[indexOfLast()] = SelectorButton.Dots
            }
        }

        val measurables : List<Measurable> = subcompose(null) {
            items.fastMap {
                when (it) {
                    SelectorButton.Backward -> PageButton(
                        onClick = { onPageSelected(page - 1) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIos,
                            contentDescription = "back"
                        )
                    }

                    SelectorButton.Dots -> PageButton() {
                        Text("...")
                    }

                    SelectorButton.Forward -> PageButton(
                        onClick = { onPageSelected(page + 1) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = "back"
                        )
                    }

                    is SelectorButton.Page -> PageButton(
                        selected = it.i == page,
                        onClick = { onPageSelected(it.i) }
                    ) {
                        Text(it.i.toString())
                    }
                }
            }
        }

        val buttonConstraints = Constraints.fixed(pageSizePx.toInt(), pageSizePx.toInt())
        val placeables = measurables.fastMap {
            it.measure(buttonConstraints)
        }

        layout(placeables.fastSumBy { it.width }, pageSizePx.toInt()) {
            var x = 0

            placeables.forEach {
                it.place(x, 0)
                x += it.width
            }
        }
    }
}

private sealed interface SelectorButton {
    object Dots : SelectorButton
    object Forward : SelectorButton
    object Backward : SelectorButton
    class Page(val i : Int): SelectorButton
}

@Composable
private fun PageButton(
    selected : Boolean = false,
    onClick: (() -> Unit)? = null,
    content : @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .size(PageSelectorSize)
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            )
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(
            LocalTextStyle.current.copy(
                lineHeight = LocalTextStyle.current.lineHeight,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onBackground
            )
        ) {
            content()
        }
    }
}

@Composable
private fun LottieCard(
    file : LottieFile,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.Url(file.lottieSource ?: file.jsonSource ?: "")
    }

    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = file.bgColor?.let(::parseColorValue) ?: Color.White
                )
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberLottiePainter(
                        composition = composition,
                        iterations = Compottie.IterateForever
                    ),
                    contentDescription = file.name
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    user = file.user,
                    size = 28.dp
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = file.user.name.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(16.dp))

                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Downloads count"
                    )
                    Text(
                        text = file.downloadCount.let {
                            if (it < 1000) {
                                it.toString()
                            } else {
                                val s = (it / 1000f).toString()
                                s.substringBefore(".") + "." + s.substringAfter(".").take(2) + "k"
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(user: User, size : Dp) {
    val placeholder = rememberVectorPainter(Icons.Default.Person)

    AsyncImage(
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        model = user.avatarUrl,
        contentDescription = user.name,
        placeholder = placeholder,
        error = placeholder
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun LottieDetails(
    modifier: Modifier = Modifier,
    onDismiss : () -> Unit,
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

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val w = (constraints.maxWidth * .9).toInt()
                    val shrinkedConstraints = constraints
                        .copy(maxWidth = w, minWidth = w)
                    val placeable = measurable.measure(shrinkedConstraints)
                    layout(constraints.maxWidth, placeable.height){
                        placeable.place((constraints.maxWidth - w)/2,0)
                    }
                }
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(
                    user = file.user,
                    size = 36.dp
                )

                Column(
                    modifier.weight(1f)
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

                IconButton(
                    onClick = onDismiss,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "close"
                    )
                }
            }
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = file.bgColor?.let(::parseColorValue) ?: Color.White
                )
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberLottiePainter(
                        composition = composition,
                        progress = animatable::value
                    ),
                    contentDescription = file.name
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying)
                            Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                }
                Slider(
                    modifier = Modifier.weight(1f),
                    value = animatable.value,
                    onValueChange = {
                        isPlaying = false
                        coroutineScope.launch {
                            animatable.snapTo(progress = it)
                        }
                    },
                    onValueChangeFinished = {
                        isPlaying = true
                    }
                )

                composition?.let {
                    Text(
                        "${(it.durationFrames * animatable.value).toInt()} / ${it.durationFrames.toInt()}"
                    )
                }

                IconButton(
                    onClick = {
                        isLooping = !isLooping
                        isPlaying = true
                    }
                ) {
                    Icon(
                        imageVector = if (isLooping)
                            Icons.Default.Repeat
                        else Icons.Default.RepeatOne,
                        contentDescription = null
                    )
                }

                IconButton(
                    onClick = {
                        speedIndex = if (speedIndex == Speed.lastIndex) {
                            0
                        } else {
                            speedIndex + 1
                        }
                    }
                ) {
                    Text(
                        text = Speed[speedIndex].second,
                        fontWeight = FontWeight.Bold,
                        lineHeight = LocalTextStyle.current.fontSize
                    )
                }
            }

            Text(
                text = "Tags",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    file.tags.fastForEach {
                        Text(
                            text = it,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 4.dp
                                )
                        )
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

private const val ALPHA_MASK = 0xFF000000.toInt()

internal fun parseColorValue(color: String): Color {

    val hex = color.lowercase().trimStart('#')

    return when (hex.length) {
        6 -> {
            // #RRGGBB
            hex.toUInt(16).toInt() or ALPHA_MASK
        }
        8 -> {
            // #AARRGGBB
            hex.toUInt(16).toInt()
        }
        3 -> {
            // #RGB
            val v = hex.toUInt(16).toInt()
            var k = (v shr 8 and 0xF) * 0x110000
            k = k or (v shr 4 and 0xF) * 0x1100
            k = k or (v and 0xF) * 0x11
            k or ALPHA_MASK
        }
        4 -> {
            // #ARGB
            val v = hex.toUInt(16).toInt()
            var k = (v shr 12 and 0xF) * 0x11000000
            k = k or (v shr 8 and 0xF) * 0x110000
            k = k or (v shr 4 and 0xF) * 0x1100
            k = k or (v and 0xF) * 0x11
            k or ALPHA_MASK
        }
        else -> ALPHA_MASK
    }.let { Color(it) }
}
