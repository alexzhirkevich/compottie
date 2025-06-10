package io.github.alexzhirkevich.compottie.example.android

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import previews.ProblematicAnimation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}

// the issue happens on emulator when using swiftshader_indirect
@Preview
@Composable
fun ProblematicAnimationAndroidPreview() {
    ProblematicAnimation(
        modifier = Modifier.size(200.dp)
    )
}