package org.jetbrains.compose.resources.vector.xmldom

import kotlin.reflect.KProperty

/**
 * XML DOM Element.
 */
internal interface Element: Node {

    fun getAttributeNS(nameSpaceURI: String, localName: String): String

    fun getAttribute(name: String): String
}
