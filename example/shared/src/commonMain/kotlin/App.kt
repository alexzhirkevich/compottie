import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compottie.example.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.NetworkAssetsManager
import io.github.alexzhirkevich.compottie.assets.ImageRepresentable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.assets.LottieFont
import io.github.alexzhirkevich.compottie.assets.LottieImage
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
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
private val CONFETTI = "confetti.json"
private val WONDERS = "wonders.json"
private val PRECOMP_WITH_REMAPPING = "precomp_with_remapping.json"
private val MASK_ADD = "mask_add.json"
private val MATTE_LUMA = "luma_matte.json"
private val DASH = "dash.json"
private val ROUNDING_CORENERS = "rounding_corners.json"
private val REPEATER = "repeater.json"
private val TEXT_WITH_PATH = "text_with_path.json"
private val TEXT = "text.json"
private val IMAGE_ASSET = "image_asset.json"
private val IMAGE_ASSET_EMBEDDED = "image_asset_embedded.json"

private val DOT = "dotlottie/dot.lottie"
private val DOT_WITH_IMAGE = "dotlottie/dot_with_image.lottie"


@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {

    val composition = rememberLottieComposition {

//        LottieCompositionSpec.DotLottie(ResourcesAssetsManager()) {
//            Res.readBytes("files/$DOT_WITH_IMAGE")
//        }
        LottieCompositionSpec.Resource(ROBOT)

//        LottieCompositionSpec.Resource(IMAGE_ASSET)

//        LottieCompositionSpec.Url(
//            url = "https://assets-v2.lottiefiles.com/a/e25360fe-1150-11ee-9d43-2f8655b815bb/xSk6HtgPaN.lottie",
//            assetsManager = NetworkAssetsManager()
//        )
    }

    LaunchedEffect(composition) {
        composition.await()
    }

    Box(contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .opacityGrid(),
            painter = rememberLottiePainter(
                composition = composition.value,
                iterations = LottieConstants.IterateForever,
            ),
            contentDescription = null
        )

        if (composition.value == null) {
            CircularProgressIndicator()
        }
    }
}

/**
 * [LottieComposition] spec from composeResources/[dir]/[path] json asset
 * */
@OptIn(ExperimentalResourceApi::class)
@Stable
fun LottieCompositionSpec.Companion.Resource(
    path : String,
    dir : String = "files",
    assetsManager: LottieAssetsManager = ResourcesAssetsManager(),
    readBytes: suspend (path: String) -> ByteArray = Res::readBytes
) : LottieCompositionSpec = JsonString(assetsManager) {
    readBytes("$dir/$path").decodeToString()
}

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
