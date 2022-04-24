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
@Suppress("TooManyFunctions")
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
    private var onPreferenceSet: (
        (
            pref: PowerfulPreference<*>?,
            key: String,
            value: String,
            cryptedKey: String,
            cryptedValue: String,
            preferenceFileName: String
        ) -> Unit
    )? = null

    /** Initialize the Prefs class for this application, with its package name as the file name, if not set. */
    @Synchronized fun init(context: Context): Builder = Builder(context.applicationContext)

    /** Builder used to initialize the library. */
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
         * Use the provided [pass] and [salt] with default crypter to encrypt/decrypt values inside SharedPreferences.
         * The default crypter uses AES algorithm and then encode/decode data in base64.
         * If [salt] is null, it will be automatically generated using SecureRandom, obfuscated using the password and
         *  saved in the same sharedPreferences file, without possibility of conflicts with current (or future) keys.
         */
        fun setCrypter(pass: String, salt: ByteArray?): Builder {
            this.password = pass
            this.salt = salt
            return this
        }

        /** Set the [prefsName] and [mode] of the sharedPreferences file. */
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

        /** Set the [logLevel] application-wide as one of [Prefs.Builder].LOG_... values. */
        fun setLogLevel(logLevel: Int): Builder {
            this.logLevel = logLevel
            return this
        }

        /**
         * Set the function [onPrefSet] to call when a preference is put into the shared preferences.
         * If no crypter is in use, encrypted key and value will be the same of key and value.
         */
        fun setOnPreferenceSet(
            onPrefSet: (
                pref: PowerfulPreference<*>?,
                key: String,
                value: String,
                cryptedKey: String,
                cryptedValue: String,
                preferenceFileName: String
            ) -> Unit
        ): Builder {
            onPreferenceSet = onPrefSet
            return this
        }

        /** Initializes the library with previous provided configuration. */
        fun build() {
            mDefaultPrefs = context.applicationContext.getSharedPreferences(defaultPrefsName, defaultPrefsMode)
            mDefaultName = defaultPrefsName
            mCacheEnabled = cacheEnabled

            // If the user set a password, I generate the default crypter and use it
            mCrypter = this.crypter
            if (password?.isNotEmpty() == true) {
                mCrypter = generateDefaultCrypter(mDefaultPrefs, password!!, salt)
            }

            prefMap.remove(defaultPrefsName)
            powerfulPrefMap.clear()
            prefMap.values.forEach { it.build(context) }

            Logger.setLevel(logLevel)
            Logger.logV(
                "Initialized with default SharedPreferences $defaultPrefsName (obfuscation: ${mCrypter != null})"
            )
            for (prefContainer in prefMap.values) {
                Logger.logV(
                    "Additional SharedPreferences files: ${prefContainer.name} " +
                        "(obfuscation: ${mCrypter != null && prefContainer.useCrypter})"
                )
            }

            // Clearing fields for security reason (memory dump)
            password = ""
            salt = ByteArray(0)
        }

        companion object {
            /** No logs, anywhere. Should be used in release builds. */
            const val LOG_DISABLED = 0

            /** Logs only errors when getting/putting values. Use in debug if you don't want to see the read values. */
            const val LOG_ERRORS = 1

            /** Logs values, keys and eventual errors. Suggested for debug builds. */
            const val LOG_VALUES = 2

            /** Logs everything, from errors (even when encrypting/decrypting), to values and keys, to other methods. */
            const val LOG_VERBOSE = 3
        }
    }

    private fun generateDefaultCrypter(prefs: SharedPreferences, pass: String, saltPassed: ByteArray?): Crypter {
        var salt = saltPassed
        if (salt == null) {
            try {
                val c = DefaultCrypter(pass, pass.toByteArray())
                var encryptedSalt = prefs.getString(c.encrypt("key") + "!", "")

                if (encryptedSalt.isNullOrEmpty()) {
                    encryptedSalt = SecureRandom().nextLong().toString() + ""
                    Logger.logD("Creating a new salt for the default crypter: $salt")
                    val encryptedSaltKey = c.encrypt("key") + "!"
                    val encryptedSaltValue = c.encrypt(encryptedSalt)
                    prefs.edit().putString(encryptedSaltKey, encryptedSaltValue).apply()
//                            onPreferenceSet?.invoke(null, encryptedSaltKey, encryptedSaltValue, mDefaultName)
                } else {
                    encryptedSalt = c.decrypt(encryptedSalt)
                }

                salt = encryptedSalt.toByteArray(charset(CHARSET_UTF8))
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Salt generation error for $saltPassed: ", e)
            }
        }
        return DefaultCrypter(pass, salt)
    }

    /**
     * Changes the values of all the preferences files, encrypting them with a new [pass] and [salt].
     * If [salt] is null, it will be automatically generated using SecureRandom, obfuscated using the password and then
     * saved in the same sharedPreferences file, without possibility of conflicts with current (or future) keys.
     * Only preferences set up to use a crypter will be changed.
     * All values are changed in a transaction: if an error occurs, none of them will be changed.
     *
     * Use the provided crypter that will be used to encrypt and decrypt values inside SharedPreferences.
     * The provided crypter uses AES algorithm and then encode/decode data in base64.
     */
    fun changeCrypter(pass: String, salt: ByteArray) {
        val newCrypter = generateDefaultCrypter(mDefaultPrefs, pass, salt)
        changeCrypter(newCrypter)
    }

    /**
     * Changes the values of all the preferences files, encrypting them with the [newCrypter].
     * If [newCrypter] is null, the current crypter will be removed, and values will be decrypted.
     * Only preferences already set up to use a crypter will be changed.
     * All values are changed in a transaction: if an error occurs, none of them will change.
     */
    @Synchronized fun changeCrypter(newCrypter: Crypter?) {
        val maps = HashMap<String, Map<String, Triple<String, String, String>>>(prefMap.size)
        if (mCacheEnabled) cacheMap.clear()

        // I update the crypter only for preferences already using a crypter
        prefMap.values.filter { it.useCrypter }.forEach { prefContainer ->
            val values = prefContainer.sharedPreferences?.all ?: return
            val newValues = HashMap<String, Triple<String, String, String>>(values.size)

            // Populate newValues map with old key/values encrypted with the new crypter
            try {
                values.keys.forEach {
                    val newKey = mCrypter?.decrypt(it) ?: it
                    val newVal = mCrypter?.decrypt(values[it].toString()) ?: values[it].toString()
                    newValues[newCrypter?.encrypt(newKey) ?: newKey] =
                        Triple(newKey, newVal, newCrypter?.encrypt(newVal) ?: newVal)
                }
            } catch (e: IllegalArgumentException) {
                Logger.logE(
                    "Trying to change crypter, but got an error:\n${e.localizedMessage}\nNo values were changed!"
                )
                return
            }

            maps[prefContainer.name] = newValues
            values.clear()

            // Clear the shared preference file
            prefContainer.sharedPreferences?.edit()?.clear()?.commit()

            // Populate the shared preference file with the values previously saved in newValues
            for (newKey in newValues.keys) {
                val newValue = newValues[newKey] ?: continue
                val newDecryptedKey = newValue.first
                val newDecryptedValue = newValue.second
                val newEncryptedValue = newValue.third

                prefContainer.sharedPreferences?.edit()?.putString(newKey, newEncryptedValue)?.apply()
                onPreferenceSet?.invoke(
                    null,
                    newDecryptedKey,
                    newDecryptedValue,
                    newKey,
                    newEncryptedValue,
                    prefContainer.name
                )
            }
        }

        mCrypter = newCrypter
        Logger.logV("Crypter was changed, and all values have been encrypted")
    }

    internal fun registerPreference(preference: PowerfulPreference<Any?>) {
        val ps = powerfulPrefMap[preference.key] ?: ArrayList()
        ps.add(WeakReference(preference))
        powerfulPrefMap[preference.key] = ps
    }

    /**
     * Observes the preferences. When a preference is changed, the function is called.
     * When preferences are cleared, the function is NOT called.
     */
    fun observe(function: (key: String, value: Any) -> Unit) = prefChangedCallbacks.add(function)

    /** Stops observing the preferences. */
    fun stopObserve(function: (key: String, value: Any) -> Unit) = prefChangedCallbacks.remove(function)

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with primitive types (and their boxed types) like int, Integer, boolean, Boolean...
     *
     * It takes the [key] of the preference, the default [value] to return in case of errors and the file [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     * For Enums, use [newEnumPref].
     *
     * Note: The return type is inferred from the value type.
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
        } ?: throw IllegalArgumentException("Cannot understand preference type $value. Please, provide a valid class")

        Logger.logV("Created preference $key : ${preference.toPreferences(value)} (${preference.getClassName()})")
        return preference
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works with any object implementing [PrefObj]
     * It takes the [clazz] of the stored object, other then the [key], default [value] and [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     * For Enums, use [newEnumPref]
     *
     * Note: The return type is inferred from the value type
     */
    fun <T : PrefObj> newPref(clazz: Class<T>, key: String, value: T, prefName: String? = null): PowerfulPreference<T> {
        val preference: PowerfulPreference<T> = ObjPreference(clazz, key, value, prefName)
        Logger.logV("Created preference $key : ${preference.toPreferences(value)} (${preference.getClassName()})")
        return preference
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with Enums
     * It takes the enum [clazz], other then the [key], default [value] and [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     *
     * Note: For other types refer to [newPref]
     */
    fun <T : Enum<T>> newEnumPref(clazz: Class<T>, key: String, value: T, prefName: String? = null):
        PowerfulPreference<T> {
        val preference: PowerfulPreference<T> = EnumPreference(clazz, key, value, prefName)
        Logger.logV("Created preference $key : ${preference.toPreferences(value)} (${preference.getClassName()})")
        return preference
    }

    /**
     * Retrieves a stored preference with specified [key] and [preferenceName].
     * If the [preferenceName] is null, the default preferences file will be used.
     * If the preference value doesn't exists or is invalid returns an empty string */
    @Synchronized
    operator fun get(key: String, preferenceName: String? = null): String =
        get(DummyPreference(key, "", preferenceName))

    /** Retrieves a stored [pref]. If the preference value doesn't exists or is invalid returns its defaultValue */
    @Synchronized
    operator fun <T> get(pref: PowerfulPreference<T>): T {
        if (mCacheEnabled && cacheMap.containsKey(pref.getCacheMapKey())) {
            val value = cacheMap[pref.getCacheMapKey()] as T
            Logger.logD("Retrieved from cache ${pref.key} : ${pref.toPreferences(value)} (${pref.getClassName()}")
            return value
        }

        val value = getAndDecrypt(pref.key, pref.preferencesFileName)
        val valueToReturn: T

        if (TextUtils.isEmpty(value)) {
            Logger.logE(
                "No data found for key ${pref.key}. Returning default value: ${pref.toPreferences(pref.defaultValue)}"
            )
            valueToReturn = pref.defaultValue
        } else {
            valueToReturn = try {
                val parsed = pref.parse(value)
                Logger.logD("Retrieved ${pref.key} : $value (${pref.getClassName()})")
                parsed
            } catch (e: NumberFormatException) {
                Logger.logE(
                    "Error trying to parse ${pref.key} : $value as ${pref.getClassName()}." +
                        "\n${e.localizedMessage}\nReturning default value: ${pref.toPreferences(pref.defaultValue)}"
                )
                pref.defaultValue
            } catch (ignored: Exception) {
                Logger.logE(
                    "Don't know how to parse ${pref.key} : '$value' as ${pref.getClassName()}. " +
                        "Returning default value: ${pref.toPreferences(pref.defaultValue)}"
                )
                pref.defaultValue
            }
        }
        if (mCacheEnabled) cacheMap[pref.getCacheMapKey()] = valueToReturn
        return valueToReturn
    }

    /**
     * Stores the [value] into the preference with specified [key] and [preferenceName].
     * If [preferenceName] is null, the default preferences file is used.
     * Its [PowerfulPreference.toPreferences] method will be called to get the string to save
     */
    @Synchronized
    fun <T> put(key: String, value: T, preferenceName: String? = null) =
        put(DummyPreference(key, value, preferenceName), value?.toString() ?: "")

    /** Stores the [value] into the [pref]. Its toPreferences() method will be called to get the string to save */
    @Synchronized
    fun <T> put(pref: PowerfulPreference<T>, value: T) {
        if (mCacheEnabled) cacheMap[pref.getCacheMapKey()] = value
        if (pref is DummyPreference) cacheMap.remove(pref.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(pref.key, value as Any) }
//        preference.callOnChange(value)
        if (pref !is DummyPreference) {
            powerfulPrefMap[pref.key]
                ?.filter { it.get()?.preferencesFileName == pref.preferencesFileName }
                ?.forEach { it.get()?.callOnChange(value) }
        }
        Logger.logD("Put ${pref.key} : ${pref.toPreferences(value)} (${pref.getClassName()})")
        encryptAndPut(pref, pref.key, pref.toPreferences(value), pref.preferencesFileName)

        if (pref is DummyPreference) {
            powerfulPrefMap[pref.key]
                ?.filter { it.get()?.preferencesFileName == pref.preferencesFileName }
                ?.forEach {
                    it.get()?.callOnChange()
                }
        }
    }

    /**
     * Remove the preference with specified [key], [defaultValue] (used for callbacks), and [preferenceName].
     * If [preferenceName] is null, the default preferences file is used.
     */
    @Synchronized
    fun <T> remove(key: String, defaultValue: T, preferenceName: String? = null) =
        remove(DummyPreference(key, defaultValue, preferenceName))

    /** Remove the [preference]. */
    @Synchronized
    fun <T> remove(preference: PowerfulPreference<T>) {
        if (mCacheEnabled) cacheMap.remove(preference.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }

        powerfulPrefMap[preference.key]
            ?.filter { it.get()?.preferencesFileName == preference.preferencesFileName }
            ?.forEach { it.get()?.callOnChange(preference.defaultValue) }
//        preference.callOnChange(preference.defaultValue)
        val editor = findPref(preference.preferencesFileName).edit()

        if (findCrypter(preference.preferencesFileName) == null) {
            editor?.remove(preference.key)
        }

        editor?.remove(tryOr(preference.key) { findCrypter(preference.preferencesFileName)?.encrypt(preference.key) })
        editor?.apply()
    }

    /**
     * Returns whether the preference with the passed [key] exists in [preferenceName] file.
     * If [preferenceName] is null, the default preferences file is used.
     */
    @Synchronized
    fun <T> contains(key: String, preferenceName: String? = null): Boolean =
        contains(DummyPreference(key, null, preferenceName))

    /** Returns whether the passed [preference] exists. */
    @Synchronized
    operator fun <T> contains(preference: PowerfulPreference<T>): Boolean {
        val sharedPreferences = findPref(preference.preferencesFileName)
        val found: Boolean

        if (findCrypter(preference.preferencesFileName) == null) {
            found = sharedPreferences.contains(preference.key)
            Logger.logD("Check existence of ${preference.key}: $found")
            return found
        }

        found = sharedPreferences.contains(
            tryOr(preference.key) { findCrypter(preference.preferencesFileName)?.encrypt(preference.key) }
        )

        Logger.logD("Check existence of ${preference.key}: $found")
        return found
    }

    /**
     * Removes all the keys and values from [preferencesFileName] (If null, default preferences file will be used).
     * Returns the [SharedPreferences.Editor] for chaining. The changes have already been committed/applied.
     * @see android.content.SharedPreferences.Editor.clear
     */
    @Synchronized
    fun clear(preferencesFileName: String? = null): SharedPreferences.Editor {
        val keySet = HashSet<String>()

        if (mCacheEnabled) {
            keySet.addAll(cacheMap.keys.filter { it.startsWith("${preferencesFileName ?: mDefaultName}$") })
            keySet.forEach { cacheMap.remove(it) }
        }

        powerfulPrefMap.values.flatten().filter { it.get()?.preferencesFileName == preferencesFileName }.forEach {
            it.get()?.callOnChange(it.get()?.defaultValue)
        }

//        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }
//        preference.callOnChange(preference.defaultValue)

        Logger.logD("Clearing all the preferences")
        val editor = findPref(preferencesFileName).edit()
        editor.clear().commit()
        if (preferencesFileName == null) cacheMap.clear()
        return editor
    }

    /**
     * @return Returns a map containing all encrypted preferences.
     * If the [preferencesFileName] is null, the default preferences file will be used.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAllEncrypted(preferencesFileName: String? = null): Map<String, *> {
        Logger.logD("Retrieving all the preferences")
        return findPref(preferencesFileName).all ?: HashMap<String, Any>()
    }

    /**
     * @return Returns a map containing all decrypted preferences of the specified preference file.
     * If the [preferencesFileName] is null, the default preferences file will be used.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAll(preferencesFileName: String? = null): Map<String, *> {
        Logger.logD("Retrieving all the preferences")
        return findPref(preferencesFileName).all
            ?.map { getEncryptedAndDecrypt(it.key, preferencesFileName) }
            ?.toMap() ?: HashMap<String, Any>()
    }

    /** Decrypts the value corresponding to a key. */
    private fun getEncryptedAndDecrypt(key: String, preferencesFileName: String?): Pair<String, String> {
        val sharedPreferences = findPref(preferencesFileName)

        return try {
            val crypter = findCrypter(preferencesFileName)
            val encryptedValue = sharedPreferences.getString(key, "")

            when {
                crypter == null -> Pair(key, sharedPreferences.getString(key, "") ?: "")
                encryptedValue?.isEmpty() != false -> Pair(crypter.decrypt(key), "")
                else -> {
                    val decryptedKey = crypter.decrypt(key)
                    val value = crypter.decrypt(encryptedValue).replace(key, "")
                    Logger.logV("Retrieved $decryptedKey : $value from $key : $encryptedValue")
                    Pair(decryptedKey, value)
                }
            }
        } catch (e: IllegalArgumentException) {
            Logger.logE("Error decrypting $key \n${e.localizedMessage}")
            Pair(key, sharedPreferences.getString(key, "") ?: "")
        }
    }

    /** Decrypts the value corresponding to a key. */
    private fun getAndDecrypt(key: String, preferencesFileName: String?): String {
        val sharedPreferences = findPref(preferencesFileName)

        return try {
            val crypter = findCrypter(preferencesFileName)
            val encryptedKey = crypter?.encrypt(key) ?: return ""
            val encryptedValue = sharedPreferences.getString(encryptedKey, "")

            when {
                crypter == null -> sharedPreferences.getString(key, "") ?: ""
                encryptedValue?.isEmpty() != false -> ""
                else -> {
                    val encryptedKey = crypter.encrypt(key)
                    val value = crypter.decrypt(encryptedValue).replace(encryptedKey, "")
                    Logger.logV("Retrieved $key : $value from $encryptedKey : $encryptedValue")
                    value
                }
            }
        } catch (e: IllegalArgumentException) {
            Logger.logE("Error decrypting $key \n${e.localizedMessage}")
            sharedPreferences.getString(key, "") ?: ""
        }
    }

    /** Encrypts a value. */
    private fun encryptAndPut(preference: PowerfulPreference<*>, key: String, value: String, prefFileName: String?) {
        val sharedPreferences = findPref(prefFileName)
        val editor = sharedPreferences.edit()
        val crypter = findCrypter(prefFileName)
        if (crypter == null) {
            editor.putString(key, value.trim()).apply()
            onPreferenceSet?.invoke(
                preference,
                key,
                value.trim(),
                key,
                value.trim(),
                prefFileName ?: mDefaultName
            )
            return
        }

        try {
            val encryptedKey = crypter.encrypt(key)
            val encryptedValue = crypter.encrypt(value.trim() + encryptedKey)

            editor.putString(encryptedKey, encryptedValue).apply()

            onPreferenceSet?.invoke(
                preference,
                key,
                value.trim(),
                encryptedKey,
                encryptedValue,
                prefFileName ?: mDefaultName
            )
            Logger.logV("Saving $key : ${value.trim()} as $encryptedKey : $encryptedValue")
        } catch (e: IllegalArgumentException) {
            Logger.logE("Error encrypting $key : $value \n${e.localizedMessage}")
        }
    }

    private fun findPref(name: String?): SharedPreferences {
        if (name.isNullOrEmpty()) {
            return mDefaultPrefs
        }
        return prefMap[name]?.sharedPreferences ?: mDefaultPrefs
    }

    private fun findCrypter(name: String?): Crypter? {
        if (TextUtils.isEmpty(name)) {
            return mCrypter
        }
        return if (prefMap[name]?.useCrypter == true) mCrypter else null
    }
}

internal inline fun <T> tryOr(default: T, function: () -> T): T {
    return try {
        function.invoke()
    } catch (ignored: Exception) {
        default
    }
}
