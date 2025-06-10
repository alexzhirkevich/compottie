package previews

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.shared.generated.resources.Res

@Composable
public fun ProblematicAnimation(
    modifier: Modifier = Modifier
){
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/animation_blushing_alt.json").decodeToString()
        )
    }

    Image(
        modifier = modifier,
        painter = rememberLottiePainter(
            composition = composition.value,
            iterations = Compottie.IterateForever
        ),
        contentDescription = "Lottie animation"
    )
}