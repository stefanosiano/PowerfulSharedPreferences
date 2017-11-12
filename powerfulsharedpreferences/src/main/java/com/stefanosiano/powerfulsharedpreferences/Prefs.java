package com.stefanosiano.powerfulsharedpreferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Map;

/**
 * SharedPreferences wrapper class, with added features like encryption, logging and type safety.
 */
public final class Prefs {

    //todo handle change password (getAll, decode values, create new map with decoded keys, create new crypter, clear, putAll)

    //todo support multiple sharedPreferences, through map<name, sharedPref> and map<name, Crypter>,
    //todo          PowerfulPreference<T> having prefName
    //todo          addPrefFile(name, mode, Crypter|password, salt) and addPrefFile(name, mode) using mCrypter
    
    private static final String TAG = Prefs.class.getSimpleName();
    private final static String CHARSET_UTF8  = "UTF-8";

    private static SharedPreferences mPrefs;
    private static Crypter mCrypter;

    /** Private constructor, to be sure to have a singleton */
    private Prefs(){}

    /**
     * Initialize the Prefs class to keep a reference to the SharedPreference for this application.
     * The SharedPreference will use the package name of the application as the Key.
     *
     * @param context Context object
     */
    public static Builder init(Context context){
        return new Builder(context.getApplicationContext());
    }

     public static final class Builder {

        public static final int LOG_DISABLED = 0;
        public static final int LOG_ERRORS = 1;
        public static final int LOG_VALUES = 2;
        public static final int LOG_VERBOSE = 3;

        private Crypter crypter;
        private Context context;
        private String prefsName, password;
        private byte[] salt;
        private int mode;
        private int logLevel = LOG_DISABLED;

        Builder(Context context){
            this.context = context;
            this.mode = Context.MODE_PRIVATE;
            this.prefsName = context.getPackageName();
        }
        /**
         * Set the custom crypter that will be used to encrypt and decrypt values inside SharedPreferences.
         * Passing null will not encrypt data
         * @param crypter Interface that will be used when putting and getting data from SharedPreferences
         */
        public Builder setDefaultCrypter(Crypter crypter){
            this.crypter = crypter;
            return this;
        }

        /**
         * Use the default crypter that will be used to encrypt and decrypt values inside SharedPreferences.
         * The default crypter uses AES algorithm and then encode/decode data in base64.
         *
         * @param pass Must be non-null, and represent the string that will be used to generate the key
         *             of the encryption algorithm
         * @param salt If null, salt will be automatically created using SecureRandom and saved in
         *             the sharedPreferences (after being encrypted using given password)
         */
        public Builder setDefaultCrypter(String pass, byte[] salt){
            this.password = pass;
            this.salt = salt;
            return this;
        }

         /**
          * Set the name and the mode of the sharedPreferences file
          * @param prefsName name of the sharedPreferences file
          * @param mode mode of the sharedPreferences file
          */
        public Builder setPrefsName(String prefsName, int mode){
            this.prefsName = prefsName;
            this.mode = mode;
            return this;
        }

        public Builder setLogLevel(int logLevel){
            this.logLevel = logLevel;
            return this;
        }

        public void build(){
            mPrefs = context.getApplicationContext().getSharedPreferences(prefsName, mode);

            //If the user set a password, I generate the default crypter and use it
            if(!TextUtils.isEmpty(password)) mCrypter = generateDefaultCrypter(mPrefs, password, salt);
            else mCrypter = this.crypter;

            Logger.setLevel(logLevel, TAG);
            Logger.logBuild();
        }


        private Crypter generateDefaultCrypter(SharedPreferences prefs, String pass, byte[] salt){
            Crypter c = new DefaultCrypter(pass, pass.getBytes());

            if(salt == null) {
                try {
                    String encryptedSalt = prefs.getString(c.encrypt("key") + "!", "");

                    if(TextUtils.isEmpty(encryptedSalt)) {
                        encryptedSalt = new SecureRandom().nextLong()+"";
                        prefs.edit().putString(c.encrypt("key")+"!", c.encrypt(encryptedSalt)).apply();
                    }
                    else  encryptedSalt = c.decrypt(encryptedSalt);

                    salt = encryptedSalt.getBytes(CHARSET_UTF8);
                }
                catch (Exception e){
                    throw new RuntimeException("Salt generation error");
                }
            }
            return new DefaultCrypter(pass, salt);
        }
    }



    /** Releases data hold by this class. Call this in your {@link Application#onTerminate()} method */
    public static void terminate(){
        Logger.logTerminate();
        mPrefs = null;
        mCrypter = null;
    }

    /**
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference for Integers
     */
    public static PowerfulPreference<Integer> newPref(String key, Integer value){
        return new IPreference(key, value);
    }


    /**
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference for Longs
     */
    public static PowerfulPreference<Long> newPref(String key, Long value){
        return new LPreference(key, value);
    }


    /**
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference for Floats
     */
    public static PowerfulPreference<Float> newPref(String key, Float value){
        return new FPreference(key, value);
    }

    /**
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference for Doubles
     */
    public static PowerfulPreference<Double> newPref(String key, Double value){
        return new DPreference(key, value);
    }

    /**
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference for Booleans
     */
    public static PowerfulPreference<Boolean> newPref(String key, Boolean value){
        return new BPreference(key, value);
    }

    /**
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference for Strings
     */
    public static PowerfulPreference<String> newPref(String key, String value){
        return new SPreference(key, value);
    }


    /**
     * Retrieves a stored preference.
     *
     * @param preference Preference to get data from
     * @return The preference value if it exists and is valid, otherwise defValue.
     */
    public static <T> T get(final PowerfulPreference<T> preference) {
        return parse(preference, getAndDecrypt(preference.getKey()));
    }

    /** Parses a stored preference. */
    private static <T> T parse(final PowerfulPreference<T> preference, String value) {
        if(TextUtils.isEmpty(value)){
            Logger.logParseNotFound(preference.getKey(), preference.getDefaultValue()+"");
            return preference.getDefaultValue();
        }
        try{
            T parsed = preference.parse(value);
            Logger.logGet(preference.getKey(), value, preference.getPrefClass());
            return parsed;
        }
        catch (NumberFormatException e){
            Logger.logParseNumberException(e, preference.getKey(), preference.getDefaultValue()+"");
            return preference.getDefaultValue();
        }
        catch (Exception e){
            Logger.logParseTypeException(preference.getKey(), preference.getDefaultValue()+"");
            return preference.getDefaultValue();
        }
    }

    /**
     * Stores a preference.
     *
     * @param preference Preference to get key from
     * @param value value to store
     */
    public static <T> void put(final PowerfulPreference<T> preference, T value) {
        Logger.logPut(preference.getKey(), value+"");
        encryptAndPut(preference.getKey(), value+"");
    }


    /**
     * Retrieves a stored value.
     * This works only with primitive types (and their boxed types)
     * like int, Integer, boolean, Boolean...
     *
     * Note: The return type is inferred from the default value type!
     *
     * @param key      The key of the data to retrieve.
     * @param defValue Value to return if this preference does not exist or value is invalid.
     * @return The preference value if it exists and is valid, otherwise defValue.
     */
    public static <T> T get(final String key, final T defValue) {
        String value = getAndDecrypt(key);
        return parse(key, defValue, value);
    }


    /** Parses the retrieved value. */
    private static <T> T parse(final String key, final T defValue, String value) {
        if(TextUtils.isEmpty(value)){
            Logger.logParseNotFound(key, defValue+"");
            return defValue;
        }
        try{

            T val;
            if(defValue instanceof Integer) { val = (T) (Object) Integer.parseInt(value); Logger.logGet(key, value+"", Integer.class); return val;}
            if(defValue instanceof Long) { val = (T) (Object) Long.parseLong(value); Logger.logGet(key, value+"", Long.class); return val;}
            if(defValue instanceof Float) { val = (T) (Object) Float.parseFloat(value); Logger.logGet(key, value+"", Float.class); return val;}
            if(defValue instanceof Double) { val = (T) (Object) Double.parseDouble(value); Logger.logGet(key, value+"", Double.class); return val;}
            if(defValue instanceof Boolean) { val = (T) (Object) Boolean.parseBoolean(value); Logger.logGet(key, value+"", Boolean.class); return val;}
            if(defValue instanceof String) { val = (T) value; Logger.logGet(key, value+"", String.class); return val;}

            Logger.logParseTypeException(key, defValue+"");
            return defValue;
        }
        catch (NumberFormatException e){
            Logger.logParseNumberException(e, key, defValue+"");
            return defValue;
        }
    }

    /**
     * Stores a value.
     * This works only with primitive types (and their boxed types)
     * like int, Integer, boolean, Boolean...
     *
     * @param key   The key of the data to modify.
     * @param value The new value for the data.
     */
    public static <T> void put(final String key, final T value) {
        Logger.logPut(key, value+"");
        encryptAndPut(key, value+"");
    }


    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences.
     * @see android.content.SharedPreferences#getAll()
     */
    public static Map<String, ?> getAll() {
        Logger.logGetAll();
        return mPrefs.getAll();
    }


    /**
     * Removes a preference value.
     *
     * @param key The key of the data to remove.
     * @see android.content.SharedPreferences.Editor#remove(String)
     */
    public static void remove(final String key) {
        Logger.logRemove(key);
        SharedPreferences.Editor editor = mPrefs.edit();
        if(mCrypter == null) {
            editor.remove(key);
        }

        try{
            editor.remove(mCrypter.encrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            editor.remove(key);
        }
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
        Logger.logContains(key);
        if(mCrypter == null)
            return mPrefs.contains(key);

        try{
            return mPrefs.contains(mCrypter.encrypt(key));
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
            return mPrefs.contains(key);
        }
    }

    /**
     * Removed all the stored keys and values.
     *
     * @return the {@link SharedPreferences.Editor} for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor#clear()
     */
    public static SharedPreferences.Editor clear() {
        Logger.logClear();
        final SharedPreferences.Editor editor = mPrefs.edit().clear();
        editor.apply();
        return editor;
    }



    /** Decrypts the value corresponding to a key */
    private static String getAndDecrypt(String key) {
        if(mCrypter == null)
            mPrefs.getString(key, "");

        try{
            String encryptedKey = mCrypter.encrypt(key);
            String encryptedValue = mPrefs.getString(encryptedKey, "");
            String value = mCrypter.decrypt(encryptedValue);
            Logger.logDecrypt(key, encryptedKey, encryptedValue, value);
            return value;
        }
        catch (Exception e){
            Logger.logDecryptException(e, key);
            return mPrefs.getString(key, "");
        }
    }

    /** Encrypts a value */
    private static void encryptAndPut(String key, String value) {
        final SharedPreferences.Editor editor = mPrefs.edit();

        if(mCrypter == null) {
            editor.putString(key, value.trim()).apply();
            return;
        }

        try{
            String encryptedKey = mCrypter.encrypt(key);
            String encryptedValue = mCrypter.encrypt(value.trim());

            Logger.logEncrypt(key, encryptedKey, encryptedValue, value.trim());

            editor.putString(encryptedKey, encryptedValue).apply();
        }
        catch (Exception e){
            Logger.logEncryptException(e, key);
        }
    }

}