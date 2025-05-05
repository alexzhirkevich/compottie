
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.withSign

internal object BlurHashDecoder {

    fun decode(
        blurHash: String,
        width: Int,
        height: Int,
        punch: Float = 1f
    ): ImageBitmap {
        val numCompEnc = decode83(blurHash, 0, 1)
        val numCompX = (numCompEnc % 9) + 1
        val numCompY = (numCompEnc / 9) + 1
        require(blurHash.length == 4 + 2 * numCompX * numCompY) {
            "invalid hash"
        }
        val maxAcEnc = decode83(blurHash, 1, 2)
        val maxAc = (maxAcEnc + 1) / 166f
        val colors = Array(numCompX * numCompY) { i ->
            if (i == 0) {
                val colorEnc = decode83(blurHash, 2, 6)
                decodeDc(colorEnc)
            } else {
                val from = 4 + i * 2
                val colorEnc = decode83(blurHash, from, from + 2)
                decodeAc(colorEnc, maxAc * punch)
            }
        }

        return composeBitmap(width, height, numCompX, numCompY, colors)
    }

    fun encode(bitmap: ImageBitmap) : String {

        val base = 4

        val (componentX, componentY) = if (bitmap.width > bitmap.height)
            base to ((bitmap.height/bitmap.width.toFloat()) * base).roundToInt()
        else ((bitmap.width/bitmap.height.toFloat()) * base).roundToInt() to base

        return encode(bitmap, componentX, componentY)
    }

    fun encode(bitmap: ImageBitmap, componentX : Int, componentY : Int) : String {
        val pixmap = bitmap.toPixelMap()

        val factors = Array(componentX * componentY) { FloatArray(3) }

        for (i in 0 until componentY) {
            for (j in 0 until componentX) {

                var r = 0f
                var g = 0f
                var b = 0f
                val normalisation = if (i == 0 && j == 0) 1f else 2f
                val index = i * componentX + j

                for (x in 0 until bitmap.width) {
                    for (y in 0 until bitmap.height) {
                        val basis =
                            (normalisation * cos(PI * x * j / bitmap.width) * cos(PI * y * i / bitmap.height)).toFloat()
                        val color = pixmap.get(x = x, y = y)

                        r += basis * srgbToLinear((color.red * 255).toInt())
                        g += basis * srgbToLinear((color.green * 255).toInt())
                        b += basis * srgbToLinear((color.blue * 255).toInt())
                    }
                }

                val scale = 1f / (bitmap.width * bitmap.height)
                factors[index][0] = r * scale
                factors[index][1] = g * scale
                factors[index][2] = b * scale
            }
        }

        val hash = CharArray(1 + 1 + 4 + 2 * (factors.size - 1)) // size flag + max AC + DC + 2 * AC components
        val sizeFlag = (componentX - 1 + (componentY - 1) * 9)
        encode83(sizeFlag, 1, hash, 0)

        val maximumValue: Float

        if (factors.size > 1) {
            val actualMaximumValue = factors.maxOf { it.maxOrNull() ?: Float.NEGATIVE_INFINITY }
            val quantisedMaximumValue = floor((actualMaximumValue * 166f - 0.5f).coerceIn(0f, 82f))
            maximumValue = (quantisedMaximumValue + 1) / 166
            encode83(quantisedMaximumValue.roundToInt(), 1, hash, 1)
        } else {
            maximumValue = 1f
            encode83(0, 1, hash, 1)
        }

        encode83(encodeDc(factors[0]), 4, hash, 2)

        for (i in 1 until factors.size) {
            encode83(encodeAc(factors[i], maximumValue), 2, hash, 6 + 2 * (i - 1))
        }

        return hash.concatToString()
    }

    private fun decode83(str: String, from: Int = 0, to: Int = str.length): Int {
        var result = 0
        for (i in from until to) {
            val index = charMap[str[i]] ?: -1
            if (index != -1) {
                result = result * 83 + index
            }
        }
        return result
    }

    private fun encode83(
        value: Int,
        length: Int,
        buffer: CharArray,
        offset: Int,
    ) {
        var exp = 1
        var i = 1
        while (i <= length) {
            buffer[offset + length - i] = chars[value / exp % 83]
            i++
            exp *= 83
        }
    }

    private fun decodeDc(colorEnc: Int): FloatArray {
        val r = colorEnc shr 16
        val g = (colorEnc shr 8) and 255
        val b = colorEnc and 255
        return floatArrayOf(srgbToLinear(r), srgbToLinear(g), srgbToLinear(b))
    }

    private fun srgbToLinear(colorEnc: Int): Float = srgbToLinear[colorEnc]!!

    private fun decodeAc(value: Int, maxAc: Float): FloatArray {
        val r = value / (19 * 19)
        val g = (value / 19) % 19
        val b = value % 19
        return floatArrayOf(
            signedPow2((r - 9) / 9.0f) * maxAc,
            signedPow2((g - 9) / 9.0f) * maxAc,
            signedPow2((b - 9) / 9.0f) * maxAc
        )
    }

    private fun signPow(value: Float, exp: Float) = abs(value).pow(exp).withSign(value)

    private fun encodeAc(
        value: FloatArray,
        maximumValue: Float,
    ): Int {
        val quantR = floor((signPow(value[0] / maximumValue, 0.5f) * 9f + 9.5f).coerceIn(0f, 18f))
        val quantG = floor((signPow(value[1] / maximumValue, 0.5f) * 9f + 9.5f).coerceIn(0f, 18f))
        val quantB = floor((signPow(value[2] / maximumValue, 0.5f) * 9f + 9.5f).coerceIn(0f, 18f))
        return (quantR * 19 * 19 + quantG * 19 + quantB).roundToInt()
    }

    private fun encodeDc(value: FloatArray): Int {
        val r = linearToSrgb(value[0])
        val g = linearToSrgb(value[1])
        val b = linearToSrgb(value[2])
        return (r shl 16) + (g shl 8) + b
    }

    private fun signedPow2(value: Float) = value.pow(2f).withSign(value)

    private fun composeBitmap(
        width: Int, height: Int,
        numCompX: Int, numCompY: Int,
        colors: Array<FloatArray>
    ): ImageBitmap {
        val imageArray = IntArray(width * height)
        val cosinesX = DoubleArray(width * numCompX)
        val cosinesY = DoubleArray(height * numCompY)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (j in 0 until numCompY) {
                    for (i in 0 until numCompX) {
                        val cosX = cosinesX.getCos(i, numCompX, x, width)
                        val cosY = cosinesY.getCos(j, numCompY, y, height)
                        val basis = (cosX * cosY).toFloat()
                        val color = colors[j * numCompX + i]
                        r += color[0] * basis
                        g += color[1] * basis
                        b += color[2] * basis
                    }
                }
                imageArray[x + width * y] =
                    Color(r,g,b, colorSpace = ColorSpaces.LinearSrgb).toArgb()
            }
        }

        return ImageBitmap.fromPixmap(width, height, imageArray)
    }


    private fun DoubleArray.getCos(
        x: Int,
        numComp: Int,
        y: Int,
        size: Int
    ): Double {
        val idx = x + numComp * y
        return if (this[idx] != 0.0){
            this[idx]
        } else cos(PI * y * x / size).also {
            this[idx] = it
        }
    }

    private fun linearToSrgb(value: Float): Int {
        val v = value.coerceIn(0f, 1f)
        return if (v <= 0.0031308f) {
            (v * 12.92f * 255f + 0.5f).toInt()
        } else {
            ((1.055f * v.pow(1 / 2.4f) - 0.055f) * 255 + 0.5f).toInt()
        }
    }

    private val charMap by lazy {
        chars.mapIndexed { i, c -> c to i }.toMap()
    }

     val chars by lazy {
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~".toCharArray()
    }

    private val srgbToLinear by lazy {
        mapOf(
            0 to 0.0f,
            1 to 3.03527E-4f,
            2 to 6.07054E-4f,
            3 to 9.10581E-4f,
            4 to 0.001214108f,
            5 to 0.001517635f,
            6 to 0.001821162f,
            7 to 0.0021246888f,
            8 to 0.002428216f,
            9 to 0.002731743f,
            10 to 0.00303527f,
            11 to 0.0033465356f,
            12 to 0.003676507f,
            13 to 0.004024717f,
            14 to 0.004391442f,
            15 to 0.0047769533f,
            16 to 0.005181517f,
            17 to 0.0056053917f,
            18 to 0.0060488326f,
            19 to 0.006512091f,
            20 to 0.00699541f,
            21 to 0.0074990317f,
            22 to 0.008023192f,
            23 to 0.008568125f,
            24 to 0.009134057f,
            25 to 0.009721218f,
            26 to 0.010329823f,
            27 to 0.010960094f,
            28 to 0.011612245f,
            29 to 0.012286487f,
            30 to 0.012983031f,
            31 to 0.013702081f,
            32 to 0.014443844f,
            33 to 0.015208514f,
            34 to 0.015996292f,
            35 to 0.016807375f,
            36 to 0.017641952f,
            37 to 0.018500218f,
            38 to 0.019382361f,
            39 to 0.020288562f,
            40 to 0.02121901f,
            41 to 0.022173883f,
            42 to 0.023153365f,
            43 to 0.02415763f,
            44 to 0.025186857f,
            45 to 0.026241222f,
            46 to 0.027320892f,
            47 to 0.028426038f,
            48 to 0.029556833f,
            49 to 0.03071344f,
            50 to 0.03189603f,
            51 to 0.033104762f,
            52 to 0.034339808f,
            53 to 0.035601314f,
            54 to 0.036889445f,
            55 to 0.038204364f,
            56 to 0.039546236f,
            57 to 0.0409152f,
            58 to 0.04231141f,
            59 to 0.043735027f,
            60 to 0.045186203f,
            61 to 0.046665084f,
            62 to 0.048171822f,
            63 to 0.049706563f,
            64 to 0.051269468f,
            65 to 0.052860655f,
            66 to 0.05448028f,
            67 to 0.056128494f,
            68 to 0.057805434f,
            69 to 0.05951124f,
            70 to 0.06124607f,
            71 to 0.06301003f,
            72 to 0.06480328f,
            73 to 0.06662595f,
            74 to 0.06847818f,
            75 to 0.07036011f,
            76 to 0.07227186f,
            77 to 0.07421358f,
            78 to 0.07618539f,
            79 to 0.07818743f,
            80 to 0.08021983f,
            81 to 0.082282715f,
            82 to 0.084376216f,
            83 to 0.086500466f,
            84 to 0.088655606f,
            85 to 0.09084173f,
            86 to 0.09305898f,
            87 to 0.095307484f,
            88 to 0.09758736f,
            89 to 0.09989874f,
            90 to 0.10224175f,
            91 to 0.10461649f,
            92 to 0.10702311f,
            93 to 0.10946172f,
            94 to 0.111932434f,
            95 to 0.11443538f,
            96 to 0.11697067f,
            97 to 0.119538434f,
            98 to 0.1221388f,
            99 to 0.12477184f,
            100 to 0.1274377f,
            101 to 0.13013649f,
            102 to 0.13286833f,
            103 to 0.13563335f,
            104 to 0.13843162f,
            105 to 0.1412633f,
            106 to 0.14412849f,
            107 to 0.14702728f,
            108 to 0.1499598f,
            109 to 0.15292616f,
            110 to 0.15592647f,
            111 to 0.15896086f,
            112 to 0.1620294f,
            113 to 0.16513222f,
            114 to 0.1682694f,
            115 to 0.1714411f,
            116 to 0.17464739f,
            117 to 0.17788841f,
            118 to 0.18116423f,
            119 to 0.18447499f,
            120 to 0.18782076f,
            121 to 0.19120167f,
            122 to 0.19461781f,
            123 to 0.1980693f,
            124 to 0.20155624f,
            125 to 0.2050787f,
            126 to 0.20863685f,
            127 to 0.21223073f,
            128 to 0.21586053f,
            129 to 0.21952623f,
            130 to 0.22322798f,
            131 to 0.22696589f,
            132 to 0.23074007f,
            133 to 0.23455065f,
            134 to 0.23839766f,
            135 to 0.2422812f,
            136 to 0.2462014f,
            137 to 0.25015837f,
            138 to 0.25415218f,
            139 to 0.2581829f,
            140 to 0.26225072f,
            141 to 0.26635566f,
            142 to 0.27049786f,
            143 to 0.27467737f,
            144 to 0.27889434f,
            145 to 0.2831488f,
            146 to 0.2874409f,
            147 to 0.2917707f,
            148 to 0.29613832f,
            149 to 0.30054384f,
            150 to 0.30498737f,
            151 to 0.30946895f,
            152 to 0.31398875f,
            153 to 0.31854683f,
            154 to 0.32314324f,
            155 to 0.32777813f,
            156 to 0.33245158f,
            157 to 0.33716366f,
            158 to 0.34191445f,
            159 to 0.3467041f,
            160 to 0.3515327f,
            161 to 0.35640025f,
            162 to 0.36130688f,
            163 to 0.3662527f,
            164 to 0.37123778f,
            165 to 0.37626222f,
            166 to 0.3813261f,
            167 to 0.38642952f,
            168 to 0.39157256f,
            169 to 0.3967553f,
            170 to 0.40197787f,
            171 to 0.4072403f,
            172 to 0.4125427f,
            173 to 0.41788515f,
            174 to 0.42326775f,
            175 to 0.42869055f,
            176 to 0.4341537f,
            177 to 0.43965724f,
            178 to 0.44520125f,
            179 to 0.45078585f,
            180 to 0.45641106f,
            181 to 0.46207705f,
            182 to 0.46778384f,
            183 to 0.47353154f,
            184 to 0.47932023f,
            185 to 0.48514998f,
            186 to 0.4910209f,
            187 to 0.49693304f,
            188 to 0.5028866f,
            189 to 0.50888145f,
            190 to 0.5149178f,
            191 to 0.5209957f,
            192 to 0.5271152f,
            193 to 0.5332765f,
            194 to 0.5394796f,
            195 to 0.5457246f,
            196 to 0.5520115f,
            197 to 0.5583405f,
            198 to 0.56471163f,
            199 to 0.5711249f,
            200 to 0.5775805f,
            201 to 0.5840785f,
            202 to 0.5906189f,
            203 to 0.5972019f,
            204 to 0.6038274f,
            205 to 0.6104956f,
            206 to 0.61720663f,
            207 to 0.62396044f,
            208 to 0.6307572f,
            209 to 0.63759696f,
            210 to 0.64447975f,
            211 to 0.6514057f,
            212 to 0.65837485f,
            213 to 0.66538733f,
            214 to 0.6724432f,
            215 to 0.67954254f,
            216 to 0.68668544f,
            217 to 0.6938719f,
            218 to 0.701102f,
            219 to 0.70837593f,
            220 to 0.71569365f,
            221 to 0.72305524f,
            222 to 0.7304609f,
            223 to 0.73791057f,
            224 to 0.74540436f,
            225 to 0.7529423f,
            226 to 0.76052463f,
            227 to 0.7681513f,
            228 to 0.77582234f,
            229 to 0.7835379f,
            230 to 0.79129803f,
            231 to 0.79910284f,
            232 to 0.80695236f,
            233 to 0.8148467f,
            234 to 0.82278585f,
            235 to 0.83076996f,
            236 to 0.8387991f,
            237 to 0.8468733f,
            238 to 0.8549927f,
            239 to 0.8631573f,
            240 to 0.8713672f,
            241 to 0.87962234f,
            242 to 0.8879232f,
            243 to 0.8962694f,
            244 to 0.90466136f,
            245 to 0.9130987f,
            246 to 0.92158204f,
            247 to 0.9301109f,
            248 to 0.9386859f,
            249 to 0.9473066f,
            250 to 0.9559735f,
            251 to 0.9646863f,
            252 to 0.9734455f,
            253 to 0.9822506f,
            254 to 0.9911022f,
            255 to 1f
        )
    }
}

internal expect fun ImageBitmap.Companion.fromPixmap(
    width : Int, height : Int, colors : IntArray
) : ImageBitmap