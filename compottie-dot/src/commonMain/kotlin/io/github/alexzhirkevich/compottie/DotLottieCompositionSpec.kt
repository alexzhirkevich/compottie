package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.dot.ColorRule
import io.github.alexzhirkevich.compottie.dot.DotLottieManifest
import io.github.alexzhirkevich.compottie.dot.GradientRule
import io.github.alexzhirkevich.compottie.dot.PositionRule
import io.github.alexzhirkevich.compottie.dot.ScalarRule
import io.github.alexzhirkevich.compottie.dot.ThemeRule
import io.github.alexzhirkevich.compottie.dot.ThemeRules
import io.github.alexzhirkevich.compottie.dot.VectorRule
import io.github.alexzhirkevich.compottie.dot.toTheme
import io.github.alexzhirkevich.compottie.internal.AnimationTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okio.Path.Companion.toPath

/**
 * [LottieComposition] from a dotLottie zip archive.
 *
 * @param archive dotLottie or zip archive file
 * @param animationId animation id (if dotLottie contains multiple animations)
 * */
public fun LottieCompositionSpec.Companion.DotLottie(
    archive: ByteArray,
    animationId: String? = null
) : LottieCompositionSpec = DotLottieCompositionSpec(archive, animationId)


@OptIn(ExperimentalSerializationApi::class)
private val DotLottieJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowTrailingComma = true

    serializersModule = SerializersModule {
        polymorphic(ThemeRule::class){
            subclass(ScalarRule::class)
            subclass(ColorRule::class)
            subclass(VectorRule::class)
            subclass(PositionRule::class)
            subclass(GradientRule::class)
        }
    }
}

private class DotLottieCompositionSpec(
    private val archive : ByteArray,
    private val animationId : String?,
) : LottieCompositionSpec {

    override val key: String = "zip_${archive.contentHashCode()}_${animationId.orEmpty()}"

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(): LottieComposition {

        val fileSystem = FakeFileSystem()
        val path = "lottie".toPath()

        fileSystem.write(path) {
            write(archive)
        }

        val entries = fileSystem.listZipEntries(path)

        val zipSystem = ZipFileSystem(fileSystem, entries, path)

        val manifestPath = entries.keys.firstOrNull { it.name == "manifest.json" }

        return if (manifestPath != null) {

            val manifest = DotLottieJson.decodeFromString<DotLottieManifest>(
                zipSystem.read(manifestPath).decodeToString()
            )

            val animation = checkNotNull(manifest.animations.firstOrNull()) {
                "dotLottie animation folder is empty"
            }

            val animDir = when  {
                zipSystem.entries.any { it.key.name == "animations" && it.value.isDirectory } -> "animations"
                zipSystem.entries.any { it.key.name == "a" && it.value.isDirectory } -> "a"
                else -> error("Failed to decode .lottie file: unrecognized format")
            }

            val anim = zipSystem.read("$animDir/${animationId ?: animation.id}.json".toPath())

            val dotThemes = manifest.themes?.mapNotNull {
                val theme = runCatching<AnimationTheme> {
                    DotLottieJson.decodeFromString<ThemeRules>(
                        zipSystem.read("t/${it.id}.json".toPath()).decodeToString()
                    ).toTheme()
                }.getOrElse { return@mapNotNull null }

                it.id to theme
            }?.toMap()

            LottieComposition.parse(anim.decodeToString()).apply {
                speed = animation.speed
                if (animation.loop) {
                    iterations = Compottie.IterateForever
                }
                themes = dotThemes
                prepareAssets(
                    DotLottieAssetsManager(
                        zipSystem,
                        manifestPath.parent
                    )
                )
            }
        } else {
            val animPath = entries.keys.first { it.name.endsWith(".json", true) }
            val anim = zipSystem.read(animPath)

            LottieComposition.parse(anim.decodeToString()).apply {
                prepareAssets(
                    DotLottieAssetsManager(
                        zipFileSystem = zipSystem,
                        root = animPath.parent
                    )
                )
            }
        }
    }

    override fun hashCode(): Int {
        return 31 * (31 * archive.contentHashCode() + animationId.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        return (other as? DotLottieCompositionSpec)?.let {
            it.archive.contentEquals(archive) && it.animationId == other.animationId
        } == true
    }
}

private val ZIP_MAGIC = byteArrayOf(0x50, 0x4b, 0x03, 0x04).toList()

@InternalCompottieApi
public suspend fun ByteArray.decodeToLottieComposition(
    format: LottieAnimationFormat
) : LottieComposition = when (format) {
    LottieAnimationFormat.Json -> LottieCompositionSpec.JsonString(decodeToString()).load()
    LottieAnimationFormat.DotLottie -> LottieCompositionSpec.DotLottie(this).load()
    LottieAnimationFormat.Undefined -> if (take(4) == ZIP_MAGIC) {
        decodeToLottieComposition(LottieAnimationFormat.DotLottie)
    } else {
        decodeToLottieComposition(LottieAnimationFormat.Json)
    }
}
