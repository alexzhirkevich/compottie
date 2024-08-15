package io.github.alexzhirkevich.compottie.dynamic

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PathMatchingTest {

    @Test
    fun exactPatterns() {
        assertTrue(pathMatches(path = "a", pattern = "a"))
        assertFalse(pathMatches(path = "a/b", pattern = "a/c"))
    }

    @Test
    fun patternsWithWildcard() {
        assertTrue(pathMatches(path = "a", pattern = "*"))
        assertFalse(pathMatches(path = "a/b", pattern = "*"))
        assertTrue(pathMatches(path = "a/b", pattern = "*/*"))
        assertTrue(pathMatches(path = "a/b", pattern = "*/b"))
        assertTrue(pathMatches(path = "a/b", pattern = "a/*"))
        assertFalse(pathMatches(path = "a/b/c", pattern = "a/*"))
        assertFalse(pathMatches(path = "a/b/c", pattern = "*/*"))
    }

    @Test
    fun patternsWithGlobstar() {
        assertTrue(pathMatches(path = "a", pattern = "**"))
        assertTrue(pathMatches(path = "a/b", pattern = "**"))
        assertTrue(pathMatches(path = "a/b", pattern = "*/**"))
        assertTrue(pathMatches(path = "a/b", pattern = "**/b"))
        assertTrue(pathMatches(path = "b", pattern = "**/b"))
        assertFalse(pathMatches(path = "a/b", pattern = "**/c"))
        assertTrue(pathMatches(path = "a/b/c/d/e", pattern = "**/c/**"))
        assertTrue(pathMatches(path = "c/d/e", pattern = "**/c/**"))
        assertTrue(pathMatches(path = "a/b/c", pattern = "**/c/**"))
        assertTrue(pathMatches(path = "a/b/c", pattern = "**/c"))
        assertTrue(pathMatches(path = "a/b", pattern = "a/**"))
        assertTrue(pathMatches(path = "a/b/c", pattern = "a/**"))
    }
}
