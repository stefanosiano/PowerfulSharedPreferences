package com.stefanosiano.powerful_libraries.sharedpreferences

internal const val CRYPTER_DEPRECATION_MESSAGE =
    "Crypter gives a false sense of security, so it's been renamed to Obfuscator, to better reflect its purpose."

/** Interface used to encrypt and decrypt data in SharedPreferences. */
@Deprecated(CRYPTER_DEPRECATION_MESSAGE, replaceWith = ReplaceWith("Obfuscator"))
interface Crypter {

    /** Decrypts a String. */
    @Throws(IllegalArgumentException::class)
    fun decrypt(value: String?): String

    /** Encrypts a String. */
    @Throws(IllegalArgumentException::class)
    fun encrypt(value: String?): String
}

/** Interface used to obfuscate and deobfuscate data in SharedPreferences. */
interface Obfuscator {

    /** Deobfuscates a String. */
    @Throws(IllegalArgumentException::class)
    fun deobfuscate(value: String?): String

    /** Obfuscates a String. */
    @Throws(IllegalArgumentException::class)
    fun obfuscate(value: String?): String
}

/** Simple wrapper to keep backward compatibility with whoever is using the [Crypter] class. */
internal class CrypterToObfuscator(private val crypter: Crypter) : Obfuscator {
    override fun obfuscate(value: String?) = crypter.encrypt(value)
    override fun deobfuscate(value: String?) = crypter.decrypt(value)
}
