# Compottie 

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)
![badge-web](https://img.shields.io/badge/Platform-Web-blue)

Compose Multiplatform lottie animations. Library have similar APIs to [airbnb/lottie-compose](https://github.com/airbnb/lottie/blob/master/android-compose.md) 
including play/stop, delayed finish, repeat mode, iterations

# Installation
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/compottie/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/compottie)

```kotlin


dependencies {
    implementation("com.github.alexzhirkevich:compottie:<version>")    
}
```

# Usage

```kotlin

val lottieData : String = // ... your lottie JSON 

val composition = rememberLottieComposition(lottieData)

val progress = animateLottieCompositionAsState(composition = composition)

LottieAnimation(
    composition = composition,
    progress = { progress.value },
    modifier = Modifier.size(300.dp)
)
```
