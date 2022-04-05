package com.stefanosiano.powerful_libraries.sharedpreferences

/** Interface used to encrypt and decrypt data in SharedPreferences. */
interface Crypter {

    /** Decrypts a String. */
    @Throws(IllegalArgumentException::class)
    fun decrypt(value: String?): String

    /** Encrypts a String. */
    @Throws(IllegalArgumentException::class)
    fun encrypt(value: String?): String
}
