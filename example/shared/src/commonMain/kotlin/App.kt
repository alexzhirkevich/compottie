import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import compottie.example.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.JsonString
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
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
private val PRECOMP_WITH_REMAPPING = "files/precomp_with_remapping.json"
private val MASK_ADD = "files/mask_add.json"

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {

    val json by produceState<String?>(null){
        value = Res.readBytes(MASK_ADD).decodeToString()
    }

    if (json != null) {
        val composition = rememberLottieComposition(
            LottieCompositionSpec.JsonString(json!!)
        )

        val progress by animateLottieCompositionAsState(
            composition = composition.value,
            iterations = LottieConstants.IterateForever,
        )

        Image(
            modifier = Modifier.fillMaxSize().background(Color.LightGray),
            painter = rememberLottiePainter(
                composition = composition,
                progress = {
                    progress
                },
                onLoadError = { throw it }
            ),
            contentDescription = null
        )
    }
}



