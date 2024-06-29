//package io.github.alexzhirkevich.compottie
//
//import androidx.compose.runtime.Stable
//
///**
// * For web it is org.w3c.files.File of the file picked by user.
// * For other platforms it is [String]
// * */
//expect class FilePath
//
//interface FileReader {
//    suspend fun read(path: FilePath) : ByteArray
//}
//
///**
// * File composition spec. Usually useful for files **_picked by user_**
// *
// * @param path file path. For web it is org.w3c.files.File of the file picked by user.
// * For other platforms it is [String]
// * @param reader file reader. Defaults to ] Okio default file sys
// * @param format animation format (JSON/dotLottie) if it is known
// * */
//@Stable
//fun LottieCompositionSpec.Companion.File(
//    path: FilePath,
//    reader: FileReader = defaultFileReader(),
//    format: LottieAnimationFormat = LottieAnimationFormat.Undefined
//) : LottieCompositionSpec = FileCompositionSpec(path, reader, format)
//
//private class FileCompositionSpec(
//    private val path : FilePath,
//    private val reader: FileReader,
//    private val format: LottieAnimationFormat
//) : LottieCompositionSpec {
//
//    override val key: String?
//        get() = "file_${path.key}"
//
//    @OptIn(InternalCompottieApi::class)
//    override suspend fun load(cacheKey: Any?): LottieComposition {
//        return LottieComposition.getOrCreate(cacheKey) {
//            reader.read(path).decodeToLottieComposition(format)
//        }
//    }
//}
//
//internal expect val FilePath.key : String?
//
//@Stable
//internal expect fun defaultFileReader() : FileReader
//
