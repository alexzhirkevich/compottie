package lottiefiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlin.math.abs

@Composable
internal fun LottieFilesScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: LottieFilesViewModel = viewModel { LottieFilesViewModel() }
) {
    DisposableEffect(0) {
        val l = Compottie.logger
//        Compottie.logger = null
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

    Surface(modifier) {
        BoxWithConstraints {

            val isWideScreen = constraints.maxWidth > LocalDensity.current.run {
                400.dp.toPx()
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    SearchBar(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 12.dp),
                        viewModel = viewModel
                    )

                    var sortExpanded by rememberSaveable {
                        mutableStateOf(false)
                    }

                    val sort by viewModel.sortOrder.collectAsState()

                    Box {
                        AssistChip(
                            onClick = {
                                sortExpanded = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(if (isWideScreen) sort.name else sort.name.take(1))
                            }
                        )
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = {
                                sortExpanded = false
                            }
                        ) {
                            SortOrder.entries.forEach {
                                DropdownMenuItem(
                                    leadingIcon = if (it == sort){
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = "Selected"
                                            )
                                        }
                                    } else null,
                                    text = {
                                        Text(it.name)
                                    },
                                    onClick = {
                                        sortExpanded = false
                                        viewModel.onSortOrderChanged(it)
                                    }
                                )
                            }
                        }
                    }
                }


                val files = viewModel.files.collectAsState().value

                val gridState = rememberLazyGridState()

                LazyVerticalGrid(
                    state = gridState,
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

                val pageCount by viewModel.pageCount.collectAsState()

                AnimatedVisibility(
                    visible = pageCount > 1,
                    enter = slideInVertically { it } + expandVertically(),
                    exit = slideOutVertically { it } + shrinkVertically()
                ) {
                    PageSelector(
                        page = viewModel.page.collectAsState().value,
                        pageCount = pageCount,
                        onPageSelected = viewModel::onPageSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                }
            }
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
internal fun UserAvatar(user: User, size : Dp) {
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
