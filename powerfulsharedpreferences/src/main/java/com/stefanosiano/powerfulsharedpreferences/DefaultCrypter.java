package com.stefanosiano.powerfulsharedpreferences;

import android.os.Build;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Default class that handles encryption of SharedPreferences values.
 */

final class DefaultCrypter implements Crypter {

    private final String CHARSET_UTF8  = "UTF-8";
    private final char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    private final int LENGTH_PASS = 16;
    private final int LENGTH_KEY = 128;
    private final int ITERATION = 16;

    private Key key;

    DefaultCrypter(String pass, byte[] salt) {
        this.key = generateKey(pass, salt);
    }


    private Key generateKey(String pass, byte[] salt) {
        try {
            return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(new PBEKeySpec(
                            new StringBuilder(getIdKey(pass)).reverse().toString().toCharArray(),
                            salt,
                            ITERATION,
                            LENGTH_KEY))
                    .getEncoded(), "AES");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("KeyGeneration Exception");
        }
    }

    private synchronized String getIdKey(String pass) {
        String salt;
        String base64key;
        try {
            salt = new StringBuilder(Base64.encodeToString((Build.BRAND + Build.MODEL).getBytes(CHARSET_UTF8), Base64.NO_WRAP))
                    .reverse()
                    .substring(0, 3);

        } catch (Exception e) {
            salt = "";
            e.printStackTrace();
        }
        
        try {
            base64key = new StringBuilder(Base64.encodeToString(pass.getBytes(CHARSET_UTF8), Base64.NO_WRAP))
                    .reverse().toString();

            while(base64key.length() < LENGTH_PASS)
                base64key = base64key.concat(salt).concat(base64key);

            base64key = base64key.substring(0, LENGTH_PASS);
            return base64key;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("KeyGeneration Exception");
        }
    }

    private synchronized Cipher initCipher(int cipherMode) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(cipherMode, key, new IvParameterSpec(String.valueOf(HEX_CHARS).getBytes(CHARSET_UTF8)));
            return c;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cipher initialization Exception");
        }
    }


    @Override
    public synchronized String encrypt(String mString) throws RuntimeException {

        if (mString == null || mString.length() == 0)
            throw new RuntimeException("[Encrypt] Empty string");

        Cipher enCipher = initCipher(Cipher.ENCRYPT_MODE);
        if(enCipher == null)
            return mString;

        try {

            // Encrypt the byte data of the string
            byte[] encrypted = enCipher.doFinal(mString.getBytes(CHARSET_UTF8));

            return new String(Base64.encode(encrypted, Base64.NO_WRAP), CHARSET_UTF8);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("[Encrypt] Exception: " + e.getMessage());
        }
    }

    @Override
    public synchronized String decrypt(String mString) throws RuntimeException {

        if (mString == null || mString.length() == 0)
            throw new RuntimeException("[Decrypt] Empty string");

        Cipher deCipher = initCipher(Cipher.DECRYPT_MODE);
        if(deCipher == null)
            return mString;

        try {
            // Decrypt the byte data of the encrypted string
            byte[] decrypted = Base64.decode(mString.getBytes(CHARSET_UTF8), Base64.NO_WRAP);

            return new String(deCipher.doFinal(decrypted));

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("[Decrypt] Exception: " + e.getMessage());
        }
    }

}
