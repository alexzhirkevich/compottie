import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compottie.example.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.CompottieException
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.dynamic.DynamicStroke
import io.github.alexzhirkevich.compottie.dynamic.stroke
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.MissingResourceException

private val GRADIENT_ELLIPSE = "gradient_ellipse.json"
private val TEST = "test.json"
private val CHECKMARK = "checkmark.json"
private val FADE_BALLS = "fade_balls.json"
private val BOUNCING_BALL = "bouncing_ball.json"
private val POLYSTAR = "polystar.json"
private val RECT = "rect.json"
private val ROUND_RECT = "roundrect.json"
private val ROBOT = "robot.json"
private val ROBOT_404 = "robot_404.json"
private val ASTRONAUT = "astronaut.json"
private val ANGEL = "angel.json"
private val CONFETTI = "confetti.json"
private val WONDERS = "wonders.json"
private val PRECOMP_WITH_REMAPPING = "precomp_with_remapping.json"
private val MASK_ADD = "mask_add.json"
private val MATTE_LUMA = "luma_matte.json"
private val DASH = "dash.json"
private val ROUNDING_CORENERS = "rounding_corners.json"
private val REPEATER = "repeater.json"
private val AUTOORIENT = "autoorient.json"
private val TEXT_WITH_PATH = "text_with_path.json"
private val TEXT = "text.json"
private val IMAGE_ASSET = "image_asset.json"
private val IMAGE_ASSET_EMBEDDED = "image_asset_embedded.json"

private val DOT = "dotlottie/dot.lottie"
private val DOT_WITH_IMAGE = "dotlottie/dot_with_image.lottie"

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {

//    return LottieFontExample()

    val composition = rememberLottieComposition(
        assetsManager = remember {
            ResourcesAssetsManager()
        },
        dynamic = {
            layer("Pre-comp 1", "Head Layer") {
                transform {
                    scale {
                        val p = progress
                        val scale = if (p > .5f)
                            (1f - p) / .5f else p / .5f
                        ScaleFactor(1f - scale/1.5f,1f - scale/1.5f)
                    }
                }
            }
            shapeLayer(""){
                stroke<DynamicStroke.Gradient>("qwe"){

                }
            }
        }
    ) {
//        LottieCompositionSpec.DotLottie(ResourcesAssetsManager()) {
//            Res.readBytes("files/$DOT_WITH_IMAGE")
//        }
        LottieCompositionSpec.Resource(CHECKMARK)

//        LottieCompositionSpec.Url(
//            url = "https://assets-v2.lottiefiles.com/a/e25360fe-1150-11ee-9d43-2f8655b815bb/xSk6HtgPaN.lottie",
//            assetsManager = NetworkAssetsManager()
//        )
    }

    // If you want to be aware of loading errors
    LaunchedEffect(composition) {
        try {
            composition.await()
        } catch (t : CompottieException){
            t.printStackTrace()
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val painter  = rememberLottiePainter(
            composition = composition.value,
            iterations = LottieConstants.IterateForever,
            clipToCompositionBounds = false
        )
        Image(
            modifier = Modifier
                .fillMaxSize()
//                .opacityGrid(),
            ,painter = painter,
            contentDescription = null
        )

        if (composition.value == null) {
            CircularProgressIndicator()
        }
    }
}

private val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ':, \n"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LottieFontExample() {
    var text by remember {
        mutableStateOf("")
    }


    val add1 = "COMPOTTIE NOW HAS IT'S OWN  COMPOSE MULTIPLATFORM LOTTIE RENDERING ENGINE"

    LaunchedEffect(0) {
//        while (true) {
            listOf(add1).forEach { line ->
                line.forEach {
//                if (it == ' ') {
//                    delay(200)
//                } else {
                    delay(30)
//                }
                    text += it
                }
                delay(500)
            }
            delay(1000)
            text = ""
//        }
    }

    val fontSize = 90.dp

    val focus = remember {
        FocusRequester()
    }
    LaunchedEffect(focus) {
        focus.requestFocus()
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .focusRequester(focus),
//            .focusable(interactionSource = interactionSource),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxSize(),
            value = text,
            onValueChange = {
                text = it.uppercase().filter { it in ALPHABET }
            },
            decorationBox = {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    FlowRow(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = -fontSize / 3,
                            alignment = Alignment.CenterHorizontally
                        ),
                        verticalArrangement = Arrangement.Center
                    ) {
                        text.forEach { c ->
                            val anim = when (c) {
                                ' ' -> {
                                    Spacer(
                                        modifier = Modifier
                                            .width(fontSize * 2 / 3)
                                            .height(fontSize)
                                    )
                                    return@forEach
                                }
                                ':' -> "Colon"
                                ',' -> "Comma"
                                '\'' -> "Apostrophe"
                                else -> c.toString().uppercase()
                            }
                            Image(
                                modifier = Modifier
                                    .height(fontSize)
                                    .width(fontSize * 3 / 4f),
                                painter = rememberLottiePainter(
                                    rememberLottieComposition {
                                        LottieCompositionSpec.Resource("mobilo/$anim.json")
                                    }.value
                                ),
                                contentDescription = anim
                            )
                        }

                        Image(
                            modifier = Modifier
                                .height(fontSize)
                                .padding(fontSize / 4),
                            painter = rememberLottiePainter(
                                composition = rememberLottieComposition {
                                    LottieCompositionSpec.Resource("mobilo/BlinkingCursor.json")
                                }.value,
                                iterations = LottieConstants.IterateForever
                            ),
                            contentDescription = it.toString()
                        )
                    }
                }
            }
        )
    }
}

/**
 * [LottieComposition] spec from composeResources/[dir]/[path] json asset
 * */
@OptIn(ExperimentalResourceApi::class)
@Stable
suspend fun LottieCompositionSpec.Companion.Resource(
    path : String,
    dir : String = "files",
    readBytes: suspend (path: String) -> ByteArray = { Res.readBytes(it) }
) : LottieCompositionSpec = JsonString(readBytes("$dir/$path").decodeToString())

/**
 * Compose resources asset manager.
 *
 * Assess are stored in the _**composeResources/[relativeTo]**_ directory.
 *
 * Handles the following possible cases:
 * - path="/images/", name="image.png"
 * - path="images/", name="image.png"
 * - path="", name="/images/image.png"
 * - path="", name="images/image.png"
 * */
@OptIn(ExperimentalResourceApi::class)
private class ResourcesAssetsManager(
    private val relativeTo : String = "files",
    private val readBytes : suspend (path : String) -> ByteArray = Res::readBytes,
) : LottieAssetsManager by LottieAssetsManager.Empty {
    override suspend fun image(image: LottieImage): ImageRepresentable? {
        return try {
            val trimPath = image.path
                .removePrefix("/")
                .removeSuffix("/")
                .takeIf(String::isNotEmpty)

            val trimName = image.name
                .removePrefix("/")
                .removeSuffix("/")
                .takeIf(String::isNotEmpty)

            val fullPath = listOfNotNull(
                relativeTo.takeIf(String::isNotEmpty),
                trimPath,
                trimName
            ).joinToString("/")

            ImageRepresentable.Bytes(readBytes(fullPath))
        } catch (x: MissingResourceException) {
            null
        }
    }
}


private val DarkOpacity = Color(0xff7f7f7f)
private val LightOpacity = Color(0xffb2b2b2)
private fun Modifier.opacityGrid(cellSize : Dp = 30.dp) = drawBehind {

    val sizePx = cellSize.toPx()
    val s = Size(sizePx,sizePx)
    repeat((size.width /sizePx).toInt() + 1){ i ->
        repeat((size.height / sizePx).toInt() + 1){ j->

            drawRect(
                color = if (i % 2 ==  j % 2) DarkOpacity else LightOpacity,
                topLeft = Offset(i * sizePx, j * sizePx),
                size = s
            )
        }
    }
}
