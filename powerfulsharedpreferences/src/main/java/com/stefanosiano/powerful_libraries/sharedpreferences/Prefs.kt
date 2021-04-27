package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import java.lang.ref.WeakReference
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
    private val powerfulPrefMap = HashMap<String, ArrayList<WeakReference<PowerfulPreference<Any?>>>>()
    private val prefMap = HashMap<String, PrefContainer>()
    private val cacheMap = HashMap<String, Any?>()
    private val prefChangedCallbacks = ArrayList<(key: String, value: Any) -> Unit>()
    private var onPreferenceSet: ((pref: PowerfulPreference<*>?, key: String, value: String, cryptedKey: String, cryptedValue: String, preferenceFileName: String) -> Unit)? = null


    /** Initialize the Prefs class for this application. Its package name will be used as the default file name if not set */
    @Deprecated("Use init(Context, Builder.() -> Unit) instead, or call this method in a synchronized{} block")
    fun init(context: Context): Builder {
        return Builder(context.applicationContext)
    }

    /** Initialize the Prefs class for this application. Its package name will be used as the default file name if not set */
    fun init(context: Context, f: Builder.() -> Unit) { synchronized(this) {
        f(Builder(context.applicationContext))
    } }

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


        /**
         * Set the function to call when a preference is put into the shared preferences
         * If no crypter is in use, crypted key and value will be the same of key and value
         * @param onPrefSet function to call on a preference set
         */
        fun setOnPreferenceSet(onPrefSet: (pref: PowerfulPreference<*>?, key: String, value: String, cryptedKey: String, cryptedValue: String, preferenceFileName: String) -> Unit): Builder {
            onPreferenceSet = onPrefSet
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
            powerfulPrefMap.clear()
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
                            Logger.logCreateSalt(encryptedSalt)
                            val encryptedSaltKey = c.encrypt("key") + "!"
                            val encryptedSaltValue = c.encrypt(encryptedSalt)
                            prefs.edit().putString(encryptedSaltKey, encryptedSaltValue).apply()
//                            onPreferenceSet?.invoke(null, encryptedSaltKey, encryptedSaltValue, mDefaultName)
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
        val maps = HashMap<String, Map<String, Triple<String, String, String>>>(prefMap.size)
        if(mCacheEnabled) cacheMap.clear()

        for (prefContainer in prefMap.values) {
            //I update the crypter only for preferences already using a crypter
            if (!prefContainer.useCrypter)
                continue

            val values = prefContainer.sharedPreferences?.all ?: continue

            val newValues = HashMap<String, Triple<String, String, String>>(values.size)
            try {
                values.keys.forEach {
                    val newKey = mCrypter?.decrypt(it) ?: it
                    val newVal = mCrypter?.decrypt(values[it].toString()) ?: values[it].toString()
                    newValues[newCrypter?.encrypt(newKey)?: newKey] = Triple(newKey, newVal, newCrypter?.encrypt(newVal) ?: newVal)
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

            prefContainer.sharedPreferences?.edit()?.clear()?.commit()


            maps[prefContainer.name]?.keys?.forEach {
                prefContainer.sharedPreferences?.edit()?.putString(it, maps[prefContainer.name]?.get(it)?.third)?.apply()
                onPreferenceSet?.invoke(null, maps[prefContainer.name]?.get(it)?.first?:"", maps[prefContainer.name]?.get(it)?.second?:"", it, maps[prefContainer.name]?.get(it)?.third?:"", prefContainer.name)
            }
        }

        mCrypter = newCrypter
        Logger.logChangeCrypter()
    }

    internal fun registerPreference(preference: PowerfulPreference<Any?>) {
        val ps = powerfulPrefMap.get(preference.key) ?: ArrayList()
        ps.add(WeakReference(preference))
        powerfulPrefMap.put(preference.key, ps)
    }


    /** Observes the preferences. When a preference is changed, the function is called. When preferences are cleared, the function is NOT called */
    fun observe(function: (key: String, value: Any) -> Unit) = prefChangedCallbacks.add(function)

    /** Stops observing the preferences */
    fun stopObserve(function: (key: String, value: Any) -> Unit) = prefChangedCallbacks.remove(function)


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
     * @param prefName SharedPreferences file name. If null it uses the default preferences file
     * @return An instance of PowerfulPreference
     */
    fun <T> newPref(key: String, value: T, prefName: String? = null): PowerfulPreference<T> {

        val preference: PowerfulPreference<T> = when (value) {
            is Int -> IPreference(key, value as Int, prefName) as PowerfulPreference<T>
            is Float -> FPreference(key, value as Float, prefName) as PowerfulPreference<T>
            is Double -> DPreference(key, value as Double, prefName) as PowerfulPreference<T>
            is Boolean -> BPreference(key, value as Boolean, prefName) as PowerfulPreference<T>
            is String -> SPreference(key, value as String, prefName) as PowerfulPreference<T>
            is Long -> LPreference(key, value as Long, prefName) as PowerfulPreference<T>
            else -> null
        } ?: throw RuntimeException("Cannot understand preference type. Please, provide a valid class")

        Logger.logNewPref(key, preference.toPreferences(value), preference.getPrefClass())
        return preference
    }


    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works with any object implementing [PrefObj]
     * For Enums, use [newEnumPref]
     *
     * Note: The return type is inferred from the value type
     *
     * @param clazz Class of the object stored
     * @param key Key of the preference
     * @param value default value to return in case of errors
     * @param prefName SharedPreferences file name. If null it uses the default preferences file
     * @return An instance of PowerfulPreference
     */
    fun <T> newPref(clazz: Class<T>, key: String, value: T, prefName: String? = null): PowerfulPreference<T> where T: PrefObj {
        val preference: PowerfulPreference<T> = ObjPreference(clazz, key, value, prefName)
        Logger.logNewPref(key, preference.toPreferences(value), preference.getPrefClass())
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
     * @param prefName SharedPreferences file name. If null it uses the default preferences file
     * @return An instance of PowerfulPreference
     */
    fun <T> newEnumPref(clazz: Class<T>, key: String, value: T, prefName: String? = null): PowerfulPreference<T> where T:Enum<T> {
        val preference: PowerfulPreference<T> = EnumPreference(clazz, key, value, prefName)
        Logger.logNewPref(key, preference.toPreferences(value), preference.getPrefClass())
        return preference
    }


    /** Retrieves a stored preference with specified [key] and [preferenceName] (If null, default preferences file is used).
     * If the preference value doesn't exists or is invalid returns an empty string */
    @Synchronized
    operator fun get(key: String, preferenceName: String? = null): String = get(DummyPreference(key, "", preferenceName))


    /** Retrieves a stored [preference]. If the preference value doesn't exists or is invalid returns its defaultValue */
    @Synchronized
    operator fun <T> get(preference: PowerfulPreference<T>): T {
        if(mCacheEnabled && cacheMap.containsKey(preference.getCacheMapKey())) {
            val value = cacheMap[preference.getCacheMapKey()] as T
            Logger.logGetCached(preference.key, preference.toPreferences(value), preference.getPrefClass())
            return value
        }

        val value = getAndDecrypt(preference.key, preference.preferencesFileName)
        val valueToReturn: T

        if (TextUtils.isEmpty(value)) {
            Logger.logParseNotFound(preference.key, preference.toPreferences(preference.defaultValue) + "")
            valueToReturn = preference.defaultValue
        } else {
            valueToReturn = try {
                val parsed = preference.parse(value)
                Logger.logGet(preference.key, value, preference.getPrefClass())
                parsed
            } catch (e: NumberFormatException) {
                Logger.logParseNumberException(e, preference.key, value, preference.toPreferences(preference.defaultValue) + "", preference.getPrefClass())
                preference.defaultValue
            } catch (e: Exception) {
                Logger.logParseTypeException(preference.key, value, preference.toPreferences(preference.defaultValue) + "", preference.getPrefClass())
                preference.defaultValue
            }

        }
        if(mCacheEnabled) cacheMap[preference.getCacheMapKey()] = valueToReturn
        return valueToReturn
    }

    /** Stores the [value] into the preference with specified [key] and [preferenceName] (If null, default preferences file is used).
     *  Its toPreferences() method will be called to get the string to save */
    @Synchronized
    fun <T> put(key: String, value: T, preferenceName: String? = null) { return put( DummyPreference(key, value, preferenceName), value?.toString() ?: "" ) }

    /** Stores the [value] into the [preference]. Its toPreferences() method will be called to get the string to save */
    @Synchronized
    fun <T> put(preference: PowerfulPreference<T>, value: T) {
        if(mCacheEnabled) cacheMap[preference.getCacheMapKey()] = value
        if(preference is DummyPreference) cacheMap.remove(preference.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(preference.key, value as Any) }
//        preference.callOnChange(value)
        if(preference !is DummyPreference)
            powerfulPrefMap.get(preference.key)?.filter { it.get()?.preferencesFileName == preference.preferencesFileName }?.forEach {
                it.get()?.callOnChange(value)
            }
        Logger.logPut(preference.key, if(preference is EnumPreference) (value as Enum<*>).name else preference.toPreferences(value), preference.getPrefClass())
        encryptAndPut(preference, preference.key, if(preference is EnumPreference) (value as Enum<*>).name else preference.toPreferences(value), preference.preferencesFileName)

        if(preference is DummyPreference)
            powerfulPrefMap.get(preference.key)?.filter { it.get()?.preferencesFileName == preference.preferencesFileName }?.forEach {
                it.get()?.callOnChange()
            }
    }

    /** Remove the preference with specified [key], [defaultValue] (used for callbacks), and [preferenceName] (If null, default preferences file is used) */
    @Synchronized
    fun <T> remove(key: String, defaultValue: T, preferenceName: String? = null) { return remove( DummyPreference(key, defaultValue, preferenceName) ) }


    /** Remove the [preference] */
    @Synchronized
    fun <T> remove(preference: PowerfulPreference<T>) {
        if(mCacheEnabled) cacheMap.remove(preference.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }

        powerfulPrefMap.get(preference.key)?.filter { it.get()?.preferencesFileName == preference.preferencesFileName }?.forEach {
            it.get()?.callOnChange(preference.defaultValue)
        }
//        preference.callOnChange(preference.defaultValue)
        val editor = findPref(preference.preferencesFileName).edit()

        if (findCrypter(preference.preferencesFileName) == null)
            editor?.remove(preference.key)

        editor?.remove( tryOr(preference.key) { findCrypter(preference.preferencesFileName)?.encrypt(preference.key) } )
        editor?.apply()
    }


    /** Returns whether the preference with the passed [key] exists in [preferenceName] file (If null, default preferences file is used) */
    @Synchronized
    fun <T> contains(key: String, preferenceName: String? = null): Boolean = contains(DummyPreference(key, null, preferenceName))

    /** Returns whether the passed [preference] exists */
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
     * Removes all the stored keys and values from [preferencesFileName] (If null, default preferences file will be used).
     * Returns the [SharedPreferences.Editor] for chaining. The changes have already been committed/applied.
     * @see android.content.SharedPreferences.Editor.clear
     */
    @Synchronized
    fun clear(preferencesFileName: String? = null): SharedPreferences.Editor {
        val keySet = HashSet<String>()

        if(mCacheEnabled) {
            keySet.addAll(cacheMap.keys.filter { it.startsWith("${preferencesFileName ?: mDefaultName}$") })
            keySet.forEach { cacheMap.remove(it) }
        }

        powerfulPrefMap.values.flatten().filter { it.get()?.preferencesFileName == preferencesFileName }.forEach {
            it.get()?.callOnChange(it.get()?.defaultValue)
        }

//        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }
//        preference.callOnChange(preference.defaultValue)

        Logger.logClear()
        val editor = findPref(preferencesFileName).edit()
        editor.clear().commit()
        if(preferencesFileName == null) cacheMap.clear()
        return editor
    }


    /**
     * @return Returns a map containing all encrypted preferences.
     * @param preferencesFileName Name of the sharedPreferences file to search data in. If null, default preferences file will be used.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAllEncrypted(preferencesFileName: String? = null): Map<String, *> {
        Logger.logGetAll()
        return findPref(preferencesFileName).all ?: HashMap<String, Any>()
    }


    /**
     * @return Returns a map containing all decrypted preferences.
     * @param preferencesFileName Name of the sharedPreferences file to search data in. If null, default preferences file will be used.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAll(preferencesFileName: String? = null): Map<String, *> {
        Logger.logGetAll()
        return findPref(preferencesFileName).all?.map { getEncryptedPreferenceAndDecrypt(it.key, preferencesFileName) }?.toMap() ?: HashMap<String, Any>()
    }


    /** Decrypts the value corresponding to a key  */
    private fun getEncryptedPreferenceAndDecrypt(key: String, preferencesFileName: String?): Pair<String, String> {
        val sharedPreferences = findPref(preferencesFileName)
        val crypter = findCrypter(preferencesFileName) ?: return Pair(key, sharedPreferences.getString(key, "") ?: "")

        try {
            val decryptedKey = crypter.decrypt(key)
            val encryptedValue = sharedPreferences.getString(key, "") ?: return Pair(decryptedKey, "")
            if (encryptedValue.isEmpty()) return Pair(decryptedKey, "")

            val value = crypter.decrypt(encryptedValue).replace(key, "")
            Logger.logDecrypt(decryptedKey, key, encryptedValue, value)
            return Pair(decryptedKey, value)
        } catch (e: Exception) {
            Logger.logDecryptException(e, key)
            return Pair(key, sharedPreferences.getString(key, "") ?: "")
        }

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
    private fun encryptAndPut(preference: PowerfulPreference<*>, key: String, value: String, preferencesFileName: String?) {
        val sharedPreferences = findPref(preferencesFileName)
        val editor = sharedPreferences.edit()
        val crypter = findCrypter(preferencesFileName)
        if(crypter == null) {
            editor.putString(key, value.trim()).apply()
            onPreferenceSet?.invoke(preference, key, value.trim(), key, value.trim(), preferencesFileName ?: mDefaultName)
            return
        }

        try {
            val encryptedKey = crypter.encrypt(key)
            val encryptedValue = crypter.encrypt(value.trim() + encryptedKey)

            editor.putString(encryptedKey, encryptedValue).apply()

            onPreferenceSet?.invoke(preference, key, value.trim(), encryptedKey, encryptedValue, preferencesFileName ?: mDefaultName)
            Logger.logEncrypt(key, encryptedKey, encryptedValue, value.trim())
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

internal inline fun <T> tryOr(default: T, function: () -> T): T { return try{ function.invoke() } catch (e: Exception) { default } }