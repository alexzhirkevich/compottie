# Compottie 

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)
![badge-web](https://img.shields.io/badge/Platform-Web-blue)

Compose Multiplatform lottie animations. 

Small wrapper over [airbnb/lottie-compose](https://github.com/airbnb/lottie/blob/master/android-compose.md) and skottie with features like
play/stop, delayed finish, repeat/reverse, iterations.

# Installation
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/compottie/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/compottie)

```kotlin


dependencies {
    implementation("com.github.alexzhirkevich:compottie:<version>")    
}
```

# Usage

Basic usage:

```kotlin

val lottieData : String = // ... your lottie JSON 

val composition = rememberLottieComposition(lottieData)

LottieAnimation(
    composition = composition,
    modifier = Modifier.size(300.dp)
)
```

With manual progress control:
```kotlin
val composition = rememberLottieComposition(lottieData)

val progress = animateLottieCompositionAsState(composition)

LottieAnimation(
    composition = composition,
    progress = { progress.value },
    modifier = Modifier.size(300.dp)
)
```



