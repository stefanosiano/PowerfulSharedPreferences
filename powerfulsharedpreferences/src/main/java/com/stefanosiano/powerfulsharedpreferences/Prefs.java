package com.stefanosiano.powerfulsharedpreferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * SharedPreferences wrapper class, with added features like encryption, logging and type safety.
 */
public final class Prefs {

    private final static String CHARSET_UTF8  = "UTF-8";

    private static SharedPreferences mDefaultPrefs;
    private static Crypter mCrypter;
    private static HashMap<String, PrefContainer> prefMap = new HashMap<>();
    private static HashMap<String, Object> cacheMap = new HashMap<>();



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
        /** No logs, anywhere. Should be used in release builds */
        public static final int LOG_DISABLED = 0;
        /** Logs only errors when getting/putting values. Use in debug build if you don't want to be annoyed by values read */
        public static final int LOG_ERRORS = 1;
        /** Logs values, keys and eventual errors. Suggested for debug builds */
        public static final int LOG_VALUES = 2;
        /** Logs everything, from errors (even when encrypting/decrypting), to values and keys, to other methods. Useful to have everything under control */
        public static final int LOG_VERBOSE = 3;

        private Crypter crypter;
        private Context context;
        private String defaultPrefsName, password;
        private byte[] salt;
        private int defaultPrefsMode;
        private int logLevel = LOG_DISABLED;

        Builder(Context context){
            this.context = context.getApplicationContext();
            this.defaultPrefsMode = Context.MODE_PRIVATE;
            this.defaultPrefsName = context.getApplicationContext().getPackageName();
        }
        /**
         * Set the custom crypter that will be used to encrypt and decrypt values inside SharedPreferences.
         * Passing null will not encrypt data
         * @param crypter Interface that will be used when putting and getting data from SharedPreferences
         */
        public Builder setCrypter(Crypter crypter){
            this.crypter = crypter;
            return this;
        }

        /**
         * Use the provided crypter that will be used to encrypt and decrypt values inside SharedPreferences.
         * The provided crypter uses AES algorithm and then encode/decode data in base64.
         *
         * @param pass Must be non-null, and represent the string that will be used to generate the key
         *             of the encryption algorithm
         * @param salt If null, salt will be automatically created using SecureRandom and saved in
         *             the sharedPreferences (after being encrypted using given password)
         */
        public Builder setCrypter(String pass, byte[] salt){
            this.password = pass;
            this.salt = salt;
            return this;
        }

        /**
         * Set the name and the mode of the sharedPreferences file
         * @param prefsName name of the sharedPreferences file
         * @param mode mode of the sharedPreferences file
         */
        public Builder setDefaultPrefs(String prefsName, int mode){
            this.defaultPrefsName = prefsName;
            this.defaultPrefsMode = mode;
            return this;
        }

        /**
         * Add a sharedPreferences file other then the default one.
         * To set the default preferences file, call {@link #setDefaultPrefs(String, int)}.
         * If not specified, default preference file will be named as the application package name.
         *
         * Note: Adding multiple times the same name will override previous calls
         *       Also, if the same name of the defaultPreferences is provided, this call will be ignored
         *
         * @param prefsName name of the sharedPreferences file
         * @param mode mode of the sharedPreferences file
         * @param useCrypter If true, it will use the same crypter of defaultPreferences, if specified
         *                   If false, this file will not be encrypted
         */
        public Builder addPrefs(String prefsName, int mode, boolean useCrypter){
            prefMap.put(prefsName, new PrefContainer(useCrypter, prefsName, mode));
            return this;
        }


        /**
         * Set the log level application-wide
         * @param logLevel one of Prefs.Builder.LOG_... values
         */
        public Builder setLogLevel(int logLevel){
            this.logLevel = logLevel;
            return this;
        }

        /** Initializes the library with previous provided configuration */
        public void build(){
            mDefaultPrefs = context.getApplicationContext().getSharedPreferences(defaultPrefsName, defaultPrefsMode);

            //If the user set a password, I generate the default crypter and use it
            if(!TextUtils.isEmpty(password)) mCrypter = generateDefaultCrypter(mDefaultPrefs, password, salt);
            else mCrypter = this.crypter;

            prefMap.remove(defaultPrefsName);
            for(PrefContainer prefContainer : prefMap.values())
                prefContainer.build(context);

            Logger.setLevel(logLevel);
            Logger.logBuild(defaultPrefsName, mCrypter, prefMap);

            //clearing fields for security reason (memory dump)
            password = "";
            salt = new byte[0];
        }


        private Crypter generateDefaultCrypter(SharedPreferences prefs, String pass, byte[] salt){
            if(salt == null) {
                try {
                    Crypter c = new DefaultCrypter(pass, pass.getBytes());
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



    /**
     * Changes the values of all the preferences files, encrypting them with a new password and salt
     * Only preferences already using a crypter will be changed.
     * All values are changed in a transaction: if an error occurs, nothing will change, otherwise all values will change.
     *
     * Use the provided crypter that will be used to encrypt and decrypt values inside SharedPreferences.
     * The provided crypter uses AES algorithm and then encode/decode data in base64.
     *
     * @param pass Must be non-null, and represent the string that will be used to generate the key
     *             of the encryption algorithm
     * @param salt If null, salt will be automatically created using SecureRandom and saved in
     *             the sharedPreferences (after being encrypted using given password)
     */
    public static void changeCrypter(String pass, byte[] salt){
        Builder builder = new Builder(null);
        Crypter newCrypter = builder.generateDefaultCrypter(mDefaultPrefs, pass, salt);
        changeCrypter(newCrypter);
    }

    /**
     * Changes the values of all the preferences files, encrypting them with a Crypter
     * Only preferences already using a crypter will be changed.
     * All values are changed in a transaction: if an error occurs, nothing will change, otherwise all values will change.
     *
     * @param newCrypter Interface that will be used when putting and getting data from SharedPreferences
     */
    synchronized public static void changeCrypter(Crypter newCrypter){
        HashMap<String, Map<String, String>> maps = new HashMap<>(prefMap.size());
        cacheMap.clear();

        for(PrefContainer prefContainer : prefMap.values()) {
            //I update the crypter only for preferences already using a crypter
            if(!prefContainer.useCrypter)
                continue;

            Map<String, ?> values = prefContainer.sharedPreferences.getAll();
            Map<String, String> newValues = new HashMap<>(values.size());
            try {
                for (String key : values.keySet()) {
                    String newKey = mCrypter == null ? key : mCrypter.decrypt(key);
                    String newVal = mCrypter == null ? values.get(key) + "" : mCrypter.decrypt(values.get(key) + "");
                    newValues.put(newCrypter == null ? newKey : newCrypter.encrypt(newKey), newCrypter == null ? newVal : newCrypter.encrypt(newVal));
                }
            } catch (Exception e) {
                Logger.logErrorChangeCrypter(e);
                return;
            }
            maps.put(prefContainer.name, newValues);
            values.clear();
        }

        for(PrefContainer prefContainer : prefMap.values()) {
            //I update the crypter only for preferences already using a crypter
            if(!prefContainer.useCrypter)
                continue;

            prefContainer.sharedPreferences.edit().clear().commit();

            for (String key : maps.get(prefContainer.name).keySet())
                prefContainer.sharedPreferences.edit().putString(key, maps.get(prefContainer.name).get(key)).apply();
        }

        mCrypter = newCrypter;
        Logger.logChangeCrypter();
    }


    /**
     * Convenience method to easily create PowerfulPreferences of default preferences file.
     * This works only with primitive types (and their boxed types)
     * like int, Integer, boolean, Boolean...
     *
     * Note: The return type is inferred from the value type
     *
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference
     */
    public static <T> PowerfulPreference<T> newPref(String key, T value){
        return newPref(key, value, null);
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with primitive types (and their boxed types)
     * like int, Integer, boolean, Boolean...
     *
     * Note: The return type is inferred from the value type
     *
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @param prefName SharedPreferences file name (passing null will use default preferences file)
     * @return An instance of PowerfulPreference
     */
    public static <T> PowerfulPreference<T> newPref(String key, T value, String prefName){

        PowerfulPreference<T> preference = null;
        if(value instanceof Integer) preference = (PowerfulPreference<T>) new IPreference(key, (Integer) value, prefName);
        else if(value instanceof Float) preference = (PowerfulPreference<T>) new FPreference(key, (Float) value, prefName);
        else if(value instanceof Double) preference = (PowerfulPreference<T>) new DPreference(key, (Double) value, prefName);
        else if(value instanceof Boolean) preference = (PowerfulPreference<T>) new BPreference(key, (Boolean) value, prefName);
        else if(value instanceof String) preference = (PowerfulPreference<T>) new SPreference(key, (String) value, prefName);
        else if(value instanceof Long) preference = (PowerfulPreference<T>) new LPreference(key, (Long) value, prefName);

        if(preference != null) {
            Logger.logNewPref(key, value+"", preference.getPrefClass());
            return preference;
        }

        throw new RuntimeException("Cannot understand preference type. Please, provide a valid class");
    }



    /**
     * Retrieves a stored preference.
     *
     * @param preference Preference to get data from
     * @return The preference value if it exists and is valid, otherwise defValue.
     */
    synchronized public static <T> T get(final PowerfulPreference<T> preference) {
        if(cacheMap.containsKey(preference.getCacheMapKey())) {
            T val = (T) cacheMap.get(preference.getCacheMapKey());
            Logger.logGetCached(preference.getKey(), val+"", preference.getPrefClass());
            return val;
        }

        String value = getAndDecrypt(preference.getKey(), preference.getPreferencesFileName());
        T valueToReturn;

        if(TextUtils.isEmpty(value)){
            Logger.logParseNotFound(preference.getKey(), preference.getDefaultValue()+"");
            valueToReturn = preference.getDefaultValue();
        }
        else {
            try {
                T parsed = preference.parse(value);
                Logger.logGet(preference.getKey(), value, preference.getPrefClass());
                valueToReturn = parsed;
            } catch (NumberFormatException e) {
                Logger.logParseNumberException(e, preference.getKey(), value, preference.getDefaultValue() + "", preference.getPrefClass());
                valueToReturn = preference.getDefaultValue();
            } catch (Exception e) {
                Logger.logParseTypeException(preference.getKey(), value, preference.getDefaultValue() + "", preference.getPrefClass());
                valueToReturn = preference.getDefaultValue();
            }
        }
        cacheMap.put(preference.getCacheMapKey(), valueToReturn);
        return valueToReturn;
    }

    /**
     * Stores a preference.
     *
     * @param preference Preference to get key from
     * @param value value to store
     */
    synchronized public static <T> void put(final PowerfulPreference<T> preference, T value) {
        cacheMap.put(preference.getCacheMapKey(), value);
        Logger.logPut(preference.getKey(), value+"", preference.getPrefClass());
        encryptAndPut(preference.getKey(), value+"", preference.getPreferencesFileName());
    }


    /**
     * Remove a preference.
     *
     * @param preference Preference to get key from
     */
    synchronized public static <T> void remove(final PowerfulPreference<T> preference) {
        cacheMap.remove(preference.getCacheMapKey());
        SharedPreferences.Editor editor = findPref(preference.getPreferencesFileName()).edit();

        if(findCrypter(preference.getPreferencesFileName()) == null)
            editor.remove(preference.getKey());

        try{ editor.remove(findCrypter(preference.getPreferencesFileName()).encrypt(preference.getKey())); }
        catch (Exception e){ editor.remove(preference.getKey()); }
        editor.apply();
    }

    /**
     * Remove a preference.
     *
     * @param preference Preference to get key from
     */
    synchronized public static <T> boolean contains(final PowerfulPreference<T> preference) {
        SharedPreferences sharedPreferences = findPref(preference.getPreferencesFileName());
        boolean found;
        if(findCrypter(preference.getPreferencesFileName()) == null) {
            found = sharedPreferences.contains(preference.getKey());
            Logger.logContains(preference.getKey(), found);
            return found;
        }

        try{ found = sharedPreferences.contains(findCrypter(preference.getPreferencesFileName()).encrypt(preference.getKey())); }
        catch (Exception e){ found = sharedPreferences.contains(preference.getKey()); }

        Logger.logContains(preference.getKey(), found);
        return found;
    }


    /**
     * Removed all the stored keys and values from default preferences file.
     *
     * @return the {@link SharedPreferences.Editor} for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor#clear()
     */
    synchronized public static SharedPreferences.Editor clear() {
        return clear(null);
    }


    /**
     * Removed all the stored keys and values.
     *
     * @param preferencesFileName Name of the sharedPreferences file to search data in. If null, default preferences file will be used.
     * @return the {@link SharedPreferences.Editor} for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor#clear()
     */
    synchronized public static SharedPreferences.Editor clear(String preferencesFileName) {
        Set<String> keySet = new HashSet<>();
        for (String key : cacheMap.keySet()) if (key.startsWith(preferencesFileName + "$")) keySet.add(key);
        for (String key : keySet) cacheMap.remove(key);

        Logger.logClear();
        final SharedPreferences.Editor editor = findPref(preferencesFileName).edit().clear();
        editor.apply();
        return editor;
    }


    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences of default preferences file.
     * @see android.content.SharedPreferences#getAll()
     */
    public static Map<String, ?> getAll() {
        return getAll(null);
    }


    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences.
     * @param preferencesFileName Name of the sharedPreferences file to search data in. If null, default preferences file will be used.
     * @see android.content.SharedPreferences#getAll()
     */
    public static Map<String, ?> getAll(String preferencesFileName) {
        Logger.logGetAll();
        return findPref(preferencesFileName).getAll();
    }


    /** Decrypts the value corresponding to a key */
    private static String getAndDecrypt(String key, String preferencesFileName) {
        SharedPreferences sharedPreferences = findPref(preferencesFileName);
        Crypter crypter = findCrypter(preferencesFileName);

        if(crypter == null)
            return sharedPreferences.getString(key, "");

        try{
            String encryptedKey = crypter.encrypt(key);
            String encryptedValue = sharedPreferences.getString(encryptedKey, "");
            if(TextUtils.isEmpty(encryptedValue))
                return "";
            String value = crypter.decrypt(encryptedValue).replace(encryptedKey, "");
            Logger.logDecrypt(key, encryptedKey, encryptedValue, value);
            return value;
        }
        catch (Exception e){
            Logger.logDecryptException(e, key);
            return sharedPreferences.getString(key, "");
        }
    }


    /** Encrypts a value */
    private static void encryptAndPut(String key, String value, String preferencesFileName) {
        SharedPreferences sharedPreferences = findPref(preferencesFileName);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        Crypter crypter = findCrypter(preferencesFileName);

        if(crypter == null) {
            editor.putString(key, value.trim()).apply();
            return;
        }

        try{
            String encryptedKey = crypter.encrypt(key);
            String encryptedValue = crypter.encrypt(value.trim().concat(encryptedKey));

            Logger.logEncrypt(key, encryptedKey, encryptedValue, value.trim());

            editor.putString(encryptedKey, encryptedValue).apply();
        }
        catch (Exception e){
            Logger.logEncryptException(e, key, value);
        }
    }

    private static SharedPreferences findPref(String name){
        if(TextUtils.isEmpty(name))
            return mDefaultPrefs;
        return prefMap.get(name) == null || prefMap.get(name).sharedPreferences == null ? mDefaultPrefs : prefMap.get(name).sharedPreferences;
    }

    private static Crypter findCrypter(String name){
        if(TextUtils.isEmpty(name))
            return mCrypter;
        return prefMap.get(name) == null || !prefMap.get(name).useCrypter ? null : mCrypter;
    }
}