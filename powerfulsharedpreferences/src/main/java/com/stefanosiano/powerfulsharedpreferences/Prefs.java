package com.stefanosiano.powerfulsharedpreferences;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

/**
 * SharedPreferences wrapper class, with encryption.
 */
public final class Prefs {
    
    private static final String TAG = Prefs.class.getSimpleName();

    private static SharedPreferences mPrefs;
    private static Crypter mCrypter;

    private Prefs(){}

    /**
     * Initialize the Prefs class to keep a reference to the SharedPreference for this application.
     * The SharedPreference will use the package name of the application as the Key.
     *
     * @param application Application object
     */
    public static void init(Application application, String prefsName, int mode){
        mPrefs = application.getApplicationContext().getSharedPreferences(prefsName, mode);
    }
    
    public static void setCrypter(Crypter crypter){
        mCrypter = crypter;
    }
    
    public static void useDefaultCrypter(String pass, byte[] salt){
        mCrypter = new DefaultCrypter(pass, salt);
    }

    public static void destroy(){
        mPrefs = null;
        mCrypter = null;
    }



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
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, or defValue.
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
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, or defValue.
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
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, or defValue.
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
     * Returns the double that has been saved as a long raw bits value in the long preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, or defValue.
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
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, or defValue.
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
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return Returns the preference value if it exists and is valid, or defValue.
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
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
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
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
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
     * @param key   The name of the preference to modify.
     * @param value The double value to be save in the preferences.
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
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
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
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
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
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
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
     * @param key The name of the preference to remove.
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
     * @param key The name of the preference to check.
     * @return {@code true} if the storage contains this key value, {@code false} otherwise.
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


    private static String decrypt(String key) throws RuntimeException {
        try{
            return mCrypter.decrypt(mPrefs.getString(key, ""));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return mPrefs.getString(key, "");
        }
    }

    private static String encrypt(String value) throws RuntimeException {
        try{
            return mCrypter.encrypt(value.trim());
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return "";
        }
    }

}