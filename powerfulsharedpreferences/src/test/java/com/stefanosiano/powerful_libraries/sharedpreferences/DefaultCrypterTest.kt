package com.stefanosiano.powerful_libraries.sharedpreferences

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultCrypterTest : BaseTest() {

    private val crypter: Crypter = DefaultCrypter("password", "salt".toByteArray())

    @Test
    fun encryptAndDecrypt() {
        val startingText = "This is the text that is going to be encrypted and then decrypted"
        val encrypted = crypter.encrypt(startingText)
        assertNotEquals(startingText, encrypted)
        val decrypted = crypter.decrypt(encrypted)
        assertEquals(startingText, decrypted)
    }
}