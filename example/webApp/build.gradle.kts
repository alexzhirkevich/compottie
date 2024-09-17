@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.targets.js.toHex
import java.security.MessageDigest


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}



val jsAppName = project.name + "-js"
val wasmAppName = project.name + "-wasm"

kotlin {

    js {
        moduleName = jsAppName

        browser {
            commonWebpackConfig {
                outputFileName = "$jsAppName.js"
            }
        }

        binaries.executable()
    }

    wasmJs {
        moduleName = wasmAppName
        browser {
            commonWebpackConfig {
                outputFileName = "$wasmAppName.js"
            }
        }
        binaries.executable()
    }

    sourceSets {

        val webMain by creating {
            dependsOn(commonMain.get())

            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)

            dependencies {
                implementation(compose.foundation)
                implementation(project(":example:shared"))
            }
        }
    }
}


/**
./gradlew clean compatBrowserProductionDistribution
./gradlew compatBrowserDevelopmentDistribution
 */

enum class Mode(
    val jsDistTask: String,
    val wasmJsDistTask : String,
) {
    Production(
        jsDistTask = "jsBrowserDistribution",
        wasmJsDistTask = "wasmJsBrowserDistribution",
    ),
    Development(
        jsDistTask = "jsBrowserDevelopmentExecutableDistribution",
        wasmJsDistTask = "wasmJsBrowserDevelopmentExecutableDistribution",
    )
}

Mode.values().forEach {

    val hashTask = registerHashTask(it)

    registerCompatDistTask(it).configure {
        finalizedBy(hashTask)
    }
}

// append scripts for ***run tasks that doesn't perform webpack.
// for webpack tasks these scripts will be removed later
listOf(
    "js" to jsAppName,
    "wasmJs" to wasmAppName
).forEach { (sourceSet, appName) ->
    tasks.named("${sourceSet}ProcessResources").configure {
        doLast {
            buildDir
                .resolve("processedResources")
                .resolve(sourceSet)
                .resolve("main")
                .resolve("index.html")
                .apply {
                    addScriptForTag("$appName.js", false, "body")
                    if (sourceSet == "js") {
                        addScriptForTag("skiko.js", false, "head")
                    }
                }
        }
    }
}

val scriptsToRemove = listOf(
    "skiko.js",
    "$jsAppName.js",
    "$wasmAppName.js"
)

// remove scripts added in processResources phase
fun removeScripts(html : String) : String {
    var text = html

    var idx = 0

    while (idx < html.lastIndex) {
        val start = text.indexOf("<script", idx)
        if (start == -1)
            break
        val end = text.indexOf("</script>", start) + "</script>".length
        val script = text.substring(start, end)

        if (scriptsToRemove.any { it in script }) {
            text = text.substring(0, start) + text.substring(end)
        } else {
            idx = end
        }
    }
    return text
}

// compiles js and wasm bundles, and merges them into compat dir
fun registerCompatDistTask(mode : Mode): TaskProvider<Task> {
    return tasks.register("compatBrowser${mode}Distribution") {
        group = "kotlin browser"

        dependsOn(
            tasks.named(mode.jsDistTask),
            tasks.named(mode.wasmJsDistTask),
        )

        doFirst {
            val distDir = buildDir.resolve("dist")

            val compatDir = distDir
                .resolve("compat")
                .resolve("${mode.name.lowercase()}Executable")

            mkdir(compatDir)

            listOf("js", "wasmJs").forEach {
                copy {
                    from(distDir.resolve(it).resolve("${mode.name.lowercase()}Executable"))
                    into(compatDir.absolutePath)
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }
            }
        }
    }
}

// appends files hashes and script for wasm gc detection
// https://github.com/GoogleChromeLabs/wasm-feature-detect
fun registerHashTask(mode : Mode) =
    tasks.register("compat${mode}Cache") {
        doFirst {
            val distDir = buildDir
                .resolve("dist")
                .resolve("compat")
                .resolve("${mode.name.lowercase()}Executable")

            require(distDir.exists()) {
                "Project must be assembled before hashing"
            }

            val jsHashedAppFile = distDir.resolve("$jsAppName.js").hashed()
            val wasmHashedAppFile = distDir.resolve("$wasmAppName.js").hashed().apply {
                val wasmBundle = distDir.resolve("$wasmAppName.wasm").hashed()
                writeText(readText().replace("$wasmAppName.wasm", wasmBundle.name))
            }
            val skikoWasmHashedFile = distDir.resolve("skiko.wasm").hashed()
            val skikoJsHashedFile = distDir.resolve("skiko.js").apply {
                writeText(readText().replace("skiko.wasm", skikoWasmHashedFile.name))
            }.hashed()

            distDir.resolve("index.html").apply {
                writeText(removeScripts(readText()))
                addScriptForTag(
                    tag = "body",
                    source = true,
                    script = """
                        import { gc } from "https://unpkg.com/wasm-feature-detect?module";
                        const wasmGCSupported = await gc()
                        
                        if (!wasmGCSupported){
                            let skikoScript = document.createElement("script")
                            skikoScript.src = "${skikoJsHashedFile.name}"
                            document.head.appendChild(skikoScript)
                        }
                        
                        let script = document.createElement("script")
                        script.src = wasmGCSupported 
                            ? "${wasmHashedAppFile.name}" 
                            : "${jsHashedAppFile.name}"
                        document.body.appendChild(script)
                     
                    """.trimIndent()
                )
            }
        }
    }


fun File.hashed() : File {

    val hash = MessageDigest.getInstance("md5").let { d ->
        inputStream().use {
            do {
                val bytes = it.readNBytes(4096)
                d.update(bytes)
            } while (bytes.size == 4096)
        }
        d.digest().toHex()
    }
    val hashed = parentFile.resolve("$nameWithoutExtension-$hash.$extension")
    renameTo(hashed)
    return hashed
}

fun File.addScriptForTag(
    script : String,
    source : Boolean = false,
    tag : String = "body",
) {
    val text = readText()

    val s = if (source) {
        "<script type=\"module\">\n$script\n</script>\n"
    } else {
        "<script src=\"$script\"></script>\n"
    }

    writeText(
        StringBuilder(text).insert(text.indexOf("</$tag>"), s).toString()
    )
}
