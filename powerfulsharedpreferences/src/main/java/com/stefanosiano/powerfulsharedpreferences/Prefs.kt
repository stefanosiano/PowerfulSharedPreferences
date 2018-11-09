package com.stefanosiano.powerfulsharedpreferences

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import java.security.SecureRandom
import java.util.HashMap
import java.util.HashSet


/**
 * SharedPreferences wrapper class, with added features like encryption, logging and type safety.
 */
object Prefs {

    private const val CHARSET_UTF8 = "UTF-8"

    private lateinit var mDefaultPrefs: SharedPreferences
    private lateinit var mDefaultName: String
    private var mCacheEnabled: Boolean = false
    private var mCrypter: Crypter? = null
    private val prefMap = HashMap<String, PrefContainer>()
    private val cacheMap = HashMap<String, Any?>()
    private val prefChangedCallbacks = ArrayList<(key: String, value: Any) -> Unit>()


    /**
     * Initialize the Prefs class to keep a reference to the SharedPreference for this application.
     * The SharedPreference will use the package name of the application as the Key.
     *
     * @param context Context object
     */
    fun init(context: Context): Builder {
        return Builder(context.applicationContext)
    }

    class Builder internal constructor(context: Context) {

        private var crypter: Crypter? = null
        private val context = context.applicationContext
        private var defaultPrefsName = context.applicationContext.packageName
        private var password: String? = null
        private var salt: ByteArray? = null
        private var defaultPrefsMode = Context.MODE_PRIVATE
        private var cacheEnabled = true
        private var logLevel = LOG_DISABLED

        /**
         * Set the custom crypter that will be used to encrypt and decrypt values inside SharedPreferences.
         * Passing null will not encrypt data
         * @param crypter Interface that will be used when putting and getting data from SharedPreferences
         */
        fun setCrypter(crypter: Crypter): Builder {
            this.crypter = crypter
            return this
        }

        /**
         * Use the provided crypter that will be used to encrypt and decrypt values inside SharedPreferences.
         * The provided crypter uses AES algorithm and then encode/decode data in base64.
         *
         * @param pass Must be non-null, and represent the string that will be used to generate the key
         * of the encryption algorithm
         * @param salt If null, salt will be automatically created using SecureRandom and saved in
         * the sharedPreferences (after being encrypted using given password)
         */
        fun setCrypter(pass: String, salt: ByteArray?): Builder {
            this.password = pass
            this.salt = salt
            return this
        }

        /**
         * Set the name and the mode of the sharedPreferences file
         * @param prefsName name of the sharedPreferences file
         * @param mode mode of the sharedPreferences file
         */
        fun setDefaultPrefs(prefsName: String, mode: Int): Builder {
            this.defaultPrefsName = prefsName
            this.defaultPrefsMode = mode
            return this
        }

        /**
         * Disables the cache map of the preferences.
         * If this function is not called, the cache map is enabled.
         */
        fun disableCache(): Builder {
            this.cacheEnabled = false
            return this
        }

        /**
         * Add a sharedPreferences file other then the default one.
         * To set the default preferences file, call [.setDefaultPrefs].
         * If not specified, default preference file will be named as the application package name.
         *
         * Note: Adding multiple times the same name will override previous calls
         * Also, if the same name of the defaultPreferences is provided, this call will be ignored
         *
         * @param prefsName name of the sharedPreferences file
         * @param mode mode of the sharedPreferences file
         * @param useCrypter If true, it will use the same crypter of defaultPreferences, if specified
         * If false, this file will not be encrypted
         */
        fun addPrefs(prefsName: String, mode: Int, useCrypter: Boolean): Builder {
            prefMap[prefsName] = PrefContainer(useCrypter, prefsName, mode)
            return this
        }


        /**
         * Set the log level application-wide
         * @param logLevel one of Prefs.Builder.LOG_... values
         */
        fun setLogLevel(logLevel: Int): Builder {
            this.logLevel = logLevel
            return this
        }

        /** Initializes the library with previous provided configuration  */
        fun build() {
            mDefaultPrefs = context.applicationContext.getSharedPreferences(defaultPrefsName, defaultPrefsMode)
            mDefaultName = defaultPrefsName
            mCacheEnabled = cacheEnabled

            //If the user set a password, I generate the default crypter and use it
            mCrypter = if (password.isNullOrEmpty()) this.crypter
            else generateDefaultCrypter(mDefaultPrefs, password!!, salt)

            prefMap.remove(defaultPrefsName)
            prefMap.values.forEach { it.build(context) }

            Logger.setLevel(logLevel)
            Logger.logBuild(defaultPrefsName, mCrypter, prefMap)

            //clearing fields for security reason (memory dump)
            password = ""
            salt = ByteArray(0)
        }


        companion object {
            /** No logs, anywhere. Should be used in release builds  */
            val LOG_DISABLED = 0
            /** Logs only errors when getting/putting values. Use in debug build if you don't want to be annoyed by values read  */
            val LOG_ERRORS = 1
            /** Logs values, keys and eventual errors. Suggested for debug builds  */
            val LOG_VALUES = 2
            /** Logs everything, from errors (even when encrypting/decrypting), to values and keys, to other methods. Useful to have everything under control  */
            val LOG_VERBOSE = 3


            internal fun generateDefaultCrypter(prefs: SharedPreferences, pass: String, saltPassed: ByteArray?): Crypter {
                var salt = saltPassed
                if (salt == null) {
                    try {
                        val c = DefaultCrypter(pass, pass.toByteArray())
                        var encryptedSalt = prefs.getString(c.encrypt("key") + "!", "")

                        if (encryptedSalt.isNullOrEmpty()) {
                            encryptedSalt = SecureRandom().nextLong().toString() + ""
                            prefs.edit().putString(c.encrypt("key") + "!", c.encrypt(encryptedSalt)).apply()
                        } else
                            encryptedSalt = c.decrypt(encryptedSalt)

                        salt = encryptedSalt.toByteArray(charset(CHARSET_UTF8))
                    } catch (e: Exception) {
                        throw RuntimeException("Salt generation error")
                    }

                }
                return DefaultCrypter(pass, salt)
            }
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
     * of the encryption algorithm
     * @param salt If null, salt will be automatically created using SecureRandom and saved in
     * the sharedPreferences (after being encrypted using given password)
     */
    fun changeCrypter(pass: String, salt: ByteArray) {
        val newCrypter = Builder.generateDefaultCrypter(mDefaultPrefs, pass, salt)
        changeCrypter(newCrypter)
    }

    /**
     * Changes the values of all the preferences files, encrypting them with a Crypter
     * Only preferences already using a crypter will be changed.
     * All values are changed in a transaction: if an error occurs, nothing will change, otherwise all values will change.
     *
     * @param newCrypter Interface that will be used when putting and getting data from SharedPreferences. Passing null will remove current crypter
     */
    @Synchronized fun changeCrypter(newCrypter: Crypter?) {
        val maps = HashMap<String, Map<String, String>>(prefMap.size)
        if(mCacheEnabled) cacheMap.clear()

        for (prefContainer in prefMap.values) {
            //I update the crypter only for preferences already using a crypter
            if (!prefContainer.useCrypter)
                continue

            val values = prefContainer.sharedPreferences?.all ?: continue

            val newValues = HashMap<String, String>(values.size)
            try {
                values.keys.forEach {
                    val newKey = mCrypter?.decrypt(it) ?: it
                    val newVal = mCrypter?.decrypt(values[it].toString()) ?: values[it].toString()
                    newValues[newCrypter?.encrypt(newKey)?: newKey] = newCrypter?.encrypt(newVal) ?: newVal
                }
            } catch (e: Exception) {
                Logger.logErrorChangeCrypter(e)
                return
            }

            maps[prefContainer.name] = newValues
            values.clear()
        }

        for (prefContainer in prefMap.values) {
            //I update the crypter only for preferences already using a crypter
            if (!prefContainer.useCrypter) continue

            prefContainer.sharedPreferences?.edit()?.clear()?.apply()

            maps[prefContainer.name]?.keys?.forEach { prefContainer.sharedPreferences?.edit()?.putString(it, maps[prefContainer.name]?.get(it))?.apply() }
        }

        mCrypter = newCrypter
        Logger.logChangeCrypter()
    }


    /** Observes the preferences. When a preference is changed, the function is called. When preferences are cleared, the function is NOT called */
    fun observe(function: (key: String, value: Any) -> Unit) = prefChangedCallbacks.add(function)

    /** Stops observing the preferences */
    fun stopObserve(function: (key: String, value: Any) -> Unit) = prefChangedCallbacks.remove(function)


    /**
     * Convenience method to easily create PowerfulPreferences of default preferences file.
     * This works only with primitive types (and their boxed types)
     * like int, Integer, boolean, Boolean...
     * For Enums, use [newEnumPref]
     *
     * Note: The return type is inferred from the value type
     *
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference
     */
    fun <T> newPref(key: String, value: T): PowerfulPreference<T> = newPref(key, value, null)

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with primitive types (and their boxed types)
     * like int, Integer, boolean, Boolean...
     * For Enums, use [newEnumPref]
     *
     * Note: The return type is inferred from the value type
     *
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @param prefName SharedPreferences file name (passing null will use default preferences file)
     * @return An instance of PowerfulPreference
     */
    fun <T> newPref(key: String, value: T, prefName: String?): PowerfulPreference<T> {

        val preference: PowerfulPreference<T> = when (value) {
            is Int -> IPreference(key, value as Int, prefName) as PowerfulPreference<T>
            is Float -> FPreference(key, value as Float, prefName) as PowerfulPreference<T>
            is Double -> DPreference(key, value as Double, prefName) as PowerfulPreference<T>
            is Boolean -> BPreference(key, value as Boolean, prefName) as PowerfulPreference<T>
            is String -> SPreference(key, value as String, prefName) as PowerfulPreference<T>
            is Long -> LPreference(key, value as Long, prefName) as PowerfulPreference<T>
            else -> null
        } ?: throw RuntimeException("Cannot understand preference type. Please, provide a valid class")

        Logger.logNewPref(key, value.toString() + "", preference.getPrefClass())
        return preference
    }


    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with Enums
     *
     * Note: For other types refer to [newPref]
     *
     * @param clazz Class of the Enum of the preference
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @return An instance of PowerfulPreference
     */
    fun <T> newEnumPref(clazz: Class<T>, key: String, value: T): PowerfulPreference<T> where T:Enum<T> = newEnumPref(clazz, key, value, null)

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with Enums
     *
     * Note: For other types refer to [newPref]
     *
     * @param clazz Class of the Enum of the preference
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @param prefName SharedPreferences file name (passing null will use default preferences file)
     * @return An instance of PowerfulPreference
     */
    fun <T> newEnumPref(clazz: Class<T>, key: String, value: T, prefName: String?): PowerfulPreference<T> where T:Enum<T> {
        val preference: PowerfulPreference<T> = EnumPreference(clazz, key, value, prefName)
        Logger.logNewPref(key, value.toString() + "", preference.getPrefClass())
        return preference
    }


    /**
     * Retrieves a stored preference from the default preferences file.
     *
     * @param key Ket of the preference to get data from
     * @return The preference value if it exists and is valid, otherwise an empty string.
     */
    @Synchronized
    operator fun get(key: String): String = get(DummyPreference(key, "", null))


    /**
     * Retrieves a stored preference.
     *
     * @param key Ket of the preference to get data from
     * @param preferenceName name of the preferences file to use
     * @return The preference value if it exists and is valid, otherwise an empty string.
     */
    @Synchronized
    operator fun get(key: String, preferenceName: String): String = get(DummyPreference(key, "", preferenceName))


    /**
     * Retrieves a stored preference.
     *
     * @param preference Preference to get data from
     * @return The preference value if it exists and is valid, otherwise defValue.
     */
    @Synchronized
    operator fun <T> get(preference: PowerfulPreference<T>): T {
        if(mCacheEnabled && cacheMap.containsKey(preference.getCacheMapKey())) {
            val value = cacheMap[preference.getCacheMapKey()] as T
            Logger.logGetCached(preference.key, value.toString() + "", preference.getPrefClass())
            return value
        }

        val value = getAndDecrypt(preference.key, preference.preferencesFileName)
        val valueToReturn: T

        if (TextUtils.isEmpty(value)) {
            Logger.logParseNotFound(preference.key, preference.defaultValue.toString() + "")
            valueToReturn = preference.defaultValue
        } else {
            valueToReturn = try {
                val parsed = preference.parse(value)
                Logger.logGet(preference.key, value, preference.getPrefClass())
                parsed
            } catch (e: NumberFormatException) {
                Logger.logParseNumberException(e, preference.key, value, preference.defaultValue.toString() + "", preference.getPrefClass())
                preference.defaultValue
            } catch (e: Exception) {
                Logger.logParseTypeException(preference.key, value, preference.defaultValue.toString() + "", preference.getPrefClass())
                preference.defaultValue
            }

        }
        if(mCacheEnabled) cacheMap[preference.getCacheMapKey()] = valueToReturn
        return valueToReturn
    }

    /**
     * Stores a preference in the default preferences file.
     *
     * @param key Key of the preference
     * @param value value to store
     */
    @Synchronized
    fun <T> put(key: String, value: T) = put(DummyPreference(key, value, null), value?.toString() ?: "")

    /**
     * Stores a preference.
     *
     * @param key Key of the preference
     * @param value value to store
     * @param preferenceName name of the preferences file to use
     */
    @Synchronized
    fun <T> put(key: String, value: T, preferenceName: String) { return put( DummyPreference(key, value, preferenceName), value?.toString() ?: "" ) }

    /**
     * Stores a preference.
     *
     * @param preference Preference to get key from
     * @param value value to store
     */
    @Synchronized
    fun <T> put(preference: PowerfulPreference<T>, value: T) {
        if(mCacheEnabled && preference !is DummyPreference) cacheMap[preference.getCacheMapKey()] = value
        prefChangedCallbacks.forEach { it.invoke(preference.key, value as Any) }
        preference.callOnChange(value)
        Logger.logPut(preference.key, if(preference is EnumPreference) (value as Enum<*>).name else value.toString(), preference.getPrefClass())
        encryptAndPut(preference.key, if(preference is EnumPreference) (value as Enum<*>).name else value.toString(), preference.preferencesFileName)
    }


    /**
     * Remove a preference.
     *
     * @param preference Preference to get key from
     */
    @Synchronized
    fun <T> remove(preference: PowerfulPreference<T>) {
        if(mCacheEnabled) cacheMap.remove(preference.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }
        preference.callOnChange(preference.defaultValue)
        val editor = findPref(preference.preferencesFileName).edit()

        if (findCrypter(preference.preferencesFileName) == null)
            editor?.remove(preference.key)

        editor?.remove( tryOr(preference.key) { findCrypter(preference.preferencesFileName)?.encrypt(preference.key) } )
        editor?.apply()
    }

    /**
     * Remove a preference.
     *
     * @param preference Preference to get key from
     */
    @Synchronized
    operator fun <T> contains(preference: PowerfulPreference<T>): Boolean {
        val sharedPreferences = findPref(preference.preferencesFileName)
        val found: Boolean

        if (findCrypter(preference.preferencesFileName) == null) {
            found = sharedPreferences.contains(preference.key)
            Logger.logContains(preference.key, found)
            return found
        }

        found = sharedPreferences.contains( tryOr(preference.key) { findCrypter(preference.preferencesFileName)?.encrypt(preference.key) } )

        Logger.logContains(preference.key, found)
        return found
    }


    /**
     * Removed all the stored keys and values from default preferences file.
     *
     * @return the [SharedPreferences.Editor] for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor.clear
     */
    @Synchronized fun clear(): SharedPreferences.Editor = clear(null)


    /**
     * Removed all the stored keys and values.
     *
     * @param preferencesFileName Name of the sharedPreferences file to search data in. If null, default preferences file will be used.
     * @return the [SharedPreferences.Editor] for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor.clear
     */
    @Synchronized
    fun clear(preferencesFileName: String?): SharedPreferences.Editor {
        val keySet = HashSet<String>()

        if(mCacheEnabled) {
            keySet.addAll(cacheMap.keys.filter { it.startsWith("${preferencesFileName ?: mDefaultName}$") })
            keySet.forEach { cacheMap.remove(it) }
        }

//        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }
//        preference.callOnChange(preference.defaultValue)

        Logger.logClear()
        val editor = findPref(preferencesFileName).edit().clear()
        editor.apply()
        return editor
    }


    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences of default preferences file.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAll() = getAll(null)

    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences.
     * @param preferencesFileName Name of the sharedPreferences file to search data in. If null, default preferences file will be used.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAll(preferencesFileName: String?): Map<String, *> {
        Logger.logGetAll()
        return findPref(preferencesFileName).all ?: HashMap<String, Any>()
    }


    /** Decrypts the value corresponding to a key  */
    private fun getAndDecrypt(key: String, preferencesFileName: String?): String {
        val sharedPreferences = findPref(preferencesFileName)
        val crypter = findCrypter(preferencesFileName) ?: return sharedPreferences.getString(key, "") ?: ""

        try {
            val encryptedKey = crypter.encrypt(key)
            val encryptedValue = sharedPreferences.getString(encryptedKey, "") ?: return ""
            if (encryptedValue.isEmpty()) return ""

            val value = crypter.decrypt(encryptedValue).replace(encryptedKey, "")
            Logger.logDecrypt(key, encryptedKey, encryptedValue, value)
            return value
        } catch (e: Exception) {
            Logger.logDecryptException(e, key)
            return sharedPreferences.getString(key, "") ?: ""
        }

    }


    /** Encrypts a value  */
    private fun encryptAndPut(key: String, value: String, preferencesFileName: String?) {
        val sharedPreferences = findPref(preferencesFileName)
        val editor = sharedPreferences.edit()
        val crypter = findCrypter(preferencesFileName) ?: return editor.putString(key, value.trim()).apply()

        try {
            val encryptedKey = crypter.encrypt(key)
            val encryptedValue = crypter.encrypt(value.trim() + encryptedKey)

            Logger.logEncrypt(key, encryptedKey, encryptedValue, value.trim())

            editor.putString(encryptedKey, encryptedValue).apply()
        } catch (e: Exception) {
            Logger.logEncryptException(e, key, value)
        }

    }

    private fun findPref(name: String?): SharedPreferences {
        if (name.isNullOrEmpty()) return mDefaultPrefs
        return prefMap[name]?.sharedPreferences ?: mDefaultPrefs
    }

    private fun findCrypter(name: String?): Crypter? {
        if (TextUtils.isEmpty(name))
            return mCrypter
        return if( prefMap[name]?.useCrypter == true) mCrypter else null
    }
}

inline fun <T> tryOr(default: T, function: () -> T): T { return try{ function.invoke() } catch (e: Exception) { default } }