import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import compottie.example.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.assets.rememberLottieAssetsManager
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi

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
private val CONFETTI = "confetti.json"
private val PRECOMP_WITH_REMAPPING = "precomp_with_remapping.json"
private val MASK_ADD = "mask_add.json"
private val MATTE_LUMA = "luma_matte.json"
private val DASH = "dash.json"
private val ROUNDING_CORENERS = "rounding_corners.json"
private val REPEATER = "repeater.json"
private val TEXT_WITH_PATH = "text_with_path.json"
private val TEXT = "text.json"
private val IMAGE_ASSET = "image_asset.json"

@Composable
fun App() {

    val composition = rememberLottieComposition(
        LottieCompositionSpec.Resource(MATTE_LUMA)
    )

    LaunchedEffect(composition) {
        composition.await()
    }

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = rememberLottiePainter(
            composition = composition.value,
            iterations = LottieConstants.IterateForever,
            onLoadError = { throw it },
            assetManager = rememberResourcesAssetsManager()
        ),
        contentDescription = null
    )
}


/**
 * [LottieComposition] spec from composeResources/[dir]/[path] json asset
 * */
@OptIn(ExperimentalResourceApi::class)
@Stable
fun LottieCompositionSpec.Companion.Resource(
    path : String,
    dir : String = "files",
    readBytes: suspend (path: String) -> ByteArray = Res::readBytes
) : LottieCompositionSpec = JsonString { readBytes("$dir/$path").decodeToString() }

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
@Composable
private fun rememberResourcesAssetsManager(
    relativeTo : String = "files",
    readBytes : suspend (path : String) -> ByteArray = Res::readBytes,
) =
    rememberLottieAssetsManager { _, path, name ->
        val trimPath = path
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        val trimName = name
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        val fullPath = listOf(relativeTo.takeIf(String::isNotEmpty), trimPath, trimName)
            .filterNotNull()
            .joinToString("/")

        readBytes(fullPath)
    }

