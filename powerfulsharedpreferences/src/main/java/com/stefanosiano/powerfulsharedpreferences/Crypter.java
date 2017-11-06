package com.stefanosiano.powerfulsharedpreferences;

/** Interface used to encrypt and decrypt data in SharedPreferences */
public interface Crypter {

    /** Decrypts a String */
    String decrypt(String value) throws RuntimeException;
    /** Encrypts a String */
    String encrypt(String value) throws RuntimeException;
}
