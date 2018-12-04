package com.stefanosiano.powerfullibraries.sharedpreferences


/** Interface used to encrypt and decrypt data in SharedPreferences  */
interface Crypter {

    /** Decrypts a String  */
    @Throws(RuntimeException::class)
    fun decrypt(value: String?): String

    /** Encrypts a String  */
    @Throws(RuntimeException::class)
    fun encrypt(value: String?): String
}
