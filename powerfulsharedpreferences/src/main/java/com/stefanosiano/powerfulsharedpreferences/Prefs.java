package com.stefanosiano.powerfulsharedpreferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

/**
 * SharedPreferences wrapper class, with encryption.
 */
public final class Prefs {
    
    private static final String TAG = Prefs.class.getSimpleName();

    private static SharedPreferences mPrefs;
    private static Crypter mCrypter;

    /** Private constructor, to be sure to have a singleton */
    private Prefs(){}

    /**
     * Initialize the Prefs class to keep a reference to the SharedPreference for this application.
     * The SharedPreference will use the package name of the application as the Key.
     * To enable data encryption call {@link #setCrypter(Crypter)} or {@link #setDefaultCrypter(String, byte[])}
     *
     * @param context Context object
     * @param prefsName name of the sharedPreferences file
     * @param mode mode of the sharedPreferences file
     */
    public static void init(Context context, String prefsName, int mode){
        mPrefs = context.getApplicationContext().getSharedPreferences(prefsName, mode);
    }

    /**
     * Set the custom crypter that will be used to encrypt and decrypt values inside SharedPreferences.
     * Passing null will not encrypt data
     * @param crypter Interface that will be used when putting and getting data from SharedPreferences
     */
    public static void setCrypter(Crypter crypter){
        mCrypter = crypter;
    }


    /**
     * Use the default crypter that will be used to encrypt and decrypt values inside SharedPreferences.
     * The default crypter uses AES algorithm and then encode/decode data in base64.
     *
     * @param pass Must be non-null, and represent the string that will be used to generate the key
     *             of the encryption algorithm
     * @param salt If null, salt will be automatically created using
     *             Build.DEVICE + Build.BOARD + Build.HARDWARE + Build.MODEL + Build.MANUFACTURER
     */
    public static void setDefaultCrypter(String pass, byte[] salt){
        if(salt == null)
            salt = (Build.DEVICE + Build.BOARD + Build.HARDWARE + Build.MODEL + Build.MANUFACTURER).getBytes();
        mCrypter = new DefaultCrypter(pass, salt);
    }


    /** Releases data hold by this class. Call this in your {@link Application#onTerminate()} method */
    public static void destroy(){
        mPrefs = null;
        mCrypter = null;
    }




/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */

    /**
     * Retrieves a stored int value.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getInt(String, int)
     */
    public static int get(final String key, final int defValue) {
        try{
            return Integer.parseInt(decrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }


    public static <T> T get(final PowerfulPreference<T> basePreference) {
        try{
            String decrypted = decrypt(basePreference.getKey());
            return basePreference.parse(decrypted);
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return basePreference.getDefaultValue();
        }
    }

    public static IPreference newPref(String key, int value){
        return new IPreference(key, value);
    }

/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */
/* ******************************************************************************************************************************************** */




















    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences.
     * @see android.content.SharedPreferences#getAll()
     */
    public static Map<String, ?> getAll() {
        return mPrefs.getAll();
    }

    /**
     * Retrieves a stored int value.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getInt(String, int)
     */
    public static int getInt(final String key, final int defValue) {
        try{
            return Integer.parseInt(decrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }


    /**
     * Retrieves a stored boolean value.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getBoolean(String, boolean)
     */
    public static boolean getBoolean(final String key, final boolean defValue) {
        try{
            return Boolean.parseBoolean(decrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }

    /**
     * Retrieves a stored long value.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getLong(String, long)
     */
    public static long getLong(final String key, final long defValue) {
        try{
            return Long.parseLong(decrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }

    /**
     * Returns the double that has been saved as a string.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getLong(String, long)
     */
    public static double getDouble(final String key, final double defValue) {
        try{
            return Double.parseDouble(decrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }

    /**
     * Retrieves a stored float value.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getFloat(String, float)
     */
    public static float getFloat(final String key, final float defValue) {
        try{
            return Float.parseFloat(decrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }

    /**
     * Retrieves a stored String value.
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, otherwise defValue.
     * @see android.content.SharedPreferences#getString(String, String)
     */
    public static String getString(final String key, final String defValue) {
        try{
            return decrypt(key);
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return defValue;
        }
    }

    /**
     * Stores a long value.
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     * @see android.content.SharedPreferences.Editor#putLong(String, long)
     */
    public static void putLong(final String key, final long value) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, encrypt(value+""));
        editor.apply();
    }

    /**
     * Stores an integer value.
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     * @see android.content.SharedPreferences.Editor#putInt(String, int)
     */
    public static void putInt(final String key, final int value) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Stores a double value as a long raw bits value.
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     * @see android.content.SharedPreferences.Editor#putLong(String, long)
     */
    public static void putDouble(final String key, final double value) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, encrypt(value+""));
        editor.apply();
    }

    /**
     * Stores a float value.
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     * @see android.content.SharedPreferences.Editor#putFloat(String, float)
     */
    public static void putFloat(final String key, final float value) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, encrypt(value+""));
        editor.apply();
    }

    /**
     * Stores a boolean value.
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     * @see android.content.SharedPreferences.Editor#putBoolean(String, boolean)
     */
    public static void putBoolean(final String key, final boolean value) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, encrypt(value+""));
        editor.apply();
    }

    /**
     * Stores a String value.
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     * @see android.content.SharedPreferences.Editor#putString(String, String)
     */
    public static void putString(final String key, final String value) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, encrypt(value+""));
        editor.apply();
    }

    /**
     * Removes a preference value.
     *
     * @param key The key of the data to remove.
     * @see android.content.SharedPreferences.Editor#remove(String)
     */
    public static void remove(final String key) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Checks if a value is stored for the given key.
     *
     * @param key The key of the data to check.
     * @return True if the storage contains this key value, False otherwise.
     * @see android.content.SharedPreferences#contains(String)
     */
    public static boolean contains(final String key) {
        return mPrefs.contains(key);
    }

    /**
     * Removed all the stored keys and values.
     *
     * @return the {@link SharedPreferences.Editor} for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor#clear()
     */
    public static SharedPreferences.Editor clear() {
        final SharedPreferences.Editor editor = mPrefs.edit().clear();
        editor.apply();
        return editor;
    }


    /** Decrypts the value corresponding to a key */
    private static String decrypt(String key) {
        if(mCrypter == null)
            mPrefs.getString(key, "");

        try{
            return mCrypter.decrypt(mPrefs.getString(key, ""));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return mPrefs.getString(key, "");
        }
    }

    /** Encrypts a value */
    private static String encrypt(String value) {
        if(mCrypter == null)
            return value.trim();

        try{
            return mCrypter.encrypt(value.trim());
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return "";
        }
    }

}