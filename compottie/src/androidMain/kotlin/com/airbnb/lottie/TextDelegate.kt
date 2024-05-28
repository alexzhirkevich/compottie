package com.airbnb.lottie

import androidx.annotation.RestrictTo

/**
 * To replace static text in an animation at runtime, create an instance of this class and call [.setText] to
 * replace the hard coded animation text (input) with the text of your choosing (output).
 *
 *
 * Alternatively, extend this class and override [.getText] and if the text hasn't already been set
 * by [.setText] then it will call [.getText].
 */
class TextDelegate(
    private val drawable: LottieDrawable?
) {
    private val stringMap: MutableMap<String, String> = HashMap()

    private var cacheText = true

    /**
     * Override this to replace the animation text with something dynamic. This can be used for
     * translations or custom data.
     * @param layerName the name of the layer with text
     * @param input the string at the layer with text
     * @return a String to use for the specific data, by default this is the same as getText(input)
     */
    fun getText(layerName: String?, input: String): String {
        return getText(input)
    }

    /**
     * Override this to replace the animation text with something dynamic. This can be used for
     * translations or custom data.
     */
    fun getText(input: String): String {
        return input
    }

    /**
     * Update the text that will be rendered for the given input text.
     */
    fun setText(input: String, output: String) {
        stringMap[input] = output
        invalidate()
    }

    /**
     * Sets whether or not [TextDelegate] will cache (memoize) the results of getText.
     * If this isn't necessary then set it to false.
     */
    fun setCacheText(cacheText: Boolean) {
        this.cacheText = cacheText
    }

    /**
     * Invalidates a cached string with the given input.
     */
    fun invalidateText(input: String) {
        stringMap.remove(input)
        invalidate()
    }

    /**
     * Invalidates all cached strings.
     */
    fun invalidateAllText() {
        stringMap.clear()
        invalidate()
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getTextInternal(layerName: String?, input: String): String? {
        if (cacheText && stringMap.containsKey(input)) {
            return stringMap[input]
        }
        val text = getText(layerName, input)
        if (cacheText) {
            stringMap[input] = text
        }
        return text
    }

    private fun invalidate() {
        drawable?.invalidateSelf()
    }
}
