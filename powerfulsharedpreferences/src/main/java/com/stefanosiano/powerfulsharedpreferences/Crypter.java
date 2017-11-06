package com.stefanosiano.powerfulsharedpreferences;

/**
 * Created by stefano on 06/11/17.
 */

public interface Crypter {
    String decrypt(String value);
    String encrypt(String value);
}
