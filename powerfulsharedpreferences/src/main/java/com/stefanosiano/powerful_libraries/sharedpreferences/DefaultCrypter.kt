package com.stefanosiano.powerful_libraries.sharedpreferences

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val CHARSET_UTF8 = "UTF-8"
private val HEX_CHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
private const val LENGTH_KEY = 128
private const val ITERATION = 16

/**
 * Crypter that handles encryption of SharedPreferences values based on [pass] and [salt].
 * It uses AES algorithm and then encode/decode data in base64.
 */
@Suppress("TooGenericExceptionCaught")
internal class DefaultCrypter(private val pass: String, private val salt: ByteArray) : Crypter {

    private val key: Key

    init {
        this.key = generateKey(pass, salt)
    }

    /** Generates the Key used by the Cipher objects using [pass] and [salt]. */
    private fun generateKey(pass: String, salt: ByteArray): Key {
        try {
            return SecretKeySpec(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(PBEKeySpec(pass.toCharArray(), salt, ITERATION, LENGTH_KEY))
                    .encoded,
                "AES"
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("KeyGeneration Exception: ", e)
        }
    }

    /**
     * Initialize the cipher with the key created in the constructor and [cipherMode] as one of [Cipher.ENCRYPT_MODE]
     *  or [Cipher.DECRYPT_MODE].
     */
    @Synchronized private fun initCipher(cipherMode: Int): Cipher {
        try {
            val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
            c.init(cipherMode, key, IvParameterSpec(String(HEX_CHARS).toByteArray(charset(CHARSET_UTF8))))
            return c
        } catch (e: Exception) {
            throw IllegalArgumentException("Cipher initialization Exception: ", e)
        }
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun encrypt(value: String?): String {
        if (value == null || value.isEmpty()) {
            throw IllegalArgumentException("[Encrypt] Empty string")
        }

        val enCipher = initCipher(Cipher.ENCRYPT_MODE)

        try {
            // Encrypt the byte data of the string
            val encrypted = enCipher.doFinal(value.toByteArray(charset(CHARSET_UTF8)))
            return String(Base64.encode(encrypted, Base64.NO_WRAP), charset(CHARSET_UTF8))
        } catch (e: Exception) {
            throw IllegalArgumentException("[Encrypt] Exception:", e)
        }
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun decrypt(value: String?): String {
        if (value == null || value.isEmpty()) {
            throw IllegalArgumentException("[Decrypt] Empty string")
        }

        val deCipher = initCipher(Cipher.DECRYPT_MODE)

        try {
            // Decrypt the byte data of the encrypted string
            val decrypted = Base64.decode(value.toByteArray(charset(CHARSET_UTF8)), Base64.NO_WRAP)
            return String(deCipher.doFinal(decrypted))
        } catch (e: Exception) {
            throw IllegalArgumentException("[Decrypt] Exception: ", e)
        }
    }
}
