package com.stefanosiano.powerful_libraries.sharedpreferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultObfuscatorTest : BaseTest() {

    private val obfuscator: Obfuscator = DefaultObfuscator("password", "salt".toByteArray())

    @Test
    fun obfuscateAndDeobfuscate() {
        val startingText = "This is the text that is going to be obfuscated and then deobfuscated"
        val obfuscated = obfuscator.obfuscate(startingText)
        assertNotEquals(startingText, obfuscated)
        val deobfuscated = obfuscator.deobfuscate(obfuscated)
        assertEquals(startingText, deobfuscated)
    }

    @Test
    fun obfuscateAndDeobfuscateNullValuesReturnEmptyString() {
        val startingText: String? = null
        val obfuscated = obfuscator.obfuscate(startingText)
        assertEquals("", obfuscated)
        val deobfuscated = obfuscator.deobfuscate(obfuscated)
        assertEquals("", deobfuscated)
    }

    @Test
    fun obfuscateAndDeobfuscateEmptyStringReturnEmptyString() {
        val startingText = ""
        val obfuscated = obfuscator.obfuscate(startingText)
        assertEquals(startingText, obfuscated)
        val deobfuscated = obfuscator.deobfuscate(obfuscated)
        assertEquals(startingText, deobfuscated)
    }
}
