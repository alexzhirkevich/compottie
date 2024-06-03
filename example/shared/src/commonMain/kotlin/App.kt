import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import compottie.example.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val GRADIENT_ELLIPSE = "files/gradient_ellipse.json"
private val TEST = "files/test.json"
private val CHECKMARK = "files/checkmark.json"
private val FADE_BALLS = "files/fade_balls.json"
private val BOUNCING_BALL = "files/bouncing_ball.json"
private val POLYSTAR = "files/polystar.json"
private val RECT = "files/rect.json"
private val ROUND_RECT = "files/roundrect.json"
private val ROBOT = "files/robot.json"
private val ROBOT_404 = "files/robot_404.json"
private val CONFETTI = "files/confetti.json"
private val PRECOMP_WITH_REMAPPING = "files/precomp_with_remapping.json"
private val MASK_ADD = "files/mask_add.json"
private val DASH = "files/dash.json"
private val ROUNDING_CORENERS = "files/rounding_corners.json"
private val REPEATER = "files/repeater.json"
private val TEXT_WITH_PATH = "files/text_with_path.json"

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {

    val json by produceState<String?>(null){
        value = Res.readBytes(TEXT_WITH_PATH).decodeToString()
    }

    if (json != null) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.JsonString(json!!)
        )

        Image(
            modifier = Modifier.fillMaxSize().background(Color.LightGray),
            painter = rememberLottiePainter(
                composition = composition,
                iterations = LottieConstants.IterateForever
            ),
            contentDescription = null
        )
    }
}



