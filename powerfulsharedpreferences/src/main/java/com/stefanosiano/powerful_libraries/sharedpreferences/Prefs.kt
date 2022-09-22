package com.stefanosiano.powerful_libraries.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import com.stefanosiano.powerful_libraries.sharedpreferences.Prefs.toString
import org.jetbrains.annotations.TestOnly
import java.lang.ref.WeakReference
import java.security.SecureRandom
import java.util.HashMap
import java.util.HashSet

/**
 * SharedPreferences wrapper class, with added features like obfuscation, logging and type safety.
 */
@Suppress("TooManyFunctions")
object Prefs {

    private const val CHARSET_UTF8 = "UTF-8"

    private lateinit var mDefaultPrefs: SharedPreferences
    private lateinit var mDefaultName: String
    private var mCacheEnabled: Boolean = false
    private var mObfuscator: Obfuscator? = null
    private val powerfulPrefMap = HashMap<String, ArrayList<WeakReference<PowerfulPreference<Any?>>>>()
    private val prefMap = HashMap<String, PrefContainer>()
    private val cacheMap = HashMap<String, Any?>()
    private val prefChangedCallbacks = ArrayList<(key: String, value: Any) -> Unit>()
    private val logger = Logger.getInstance()
    private lateinit var prefContainerBuilder: (PrefContainer) -> Unit
    private val preInitPrefFileNames = ArrayList<String>()
    private var onPreferenceSet: (
        (
            pref: PowerfulPreference<*>?,
            key: String,
            value: String,
            obfuscatedKey: String,
            obfuscatedValue: String,
            preferenceFileName: String
        ) -> Unit
    )? = null

    /** Initialize the Prefs class for this application, with its package name as the file name, if not set. */
    @Synchronized fun init(context: Context): Builder = Builder(context.applicationContext)

    /** Builder used to initialize the library. */
    class Builder internal constructor(context: Context) {

        private var obfuscator: Obfuscator? = null
        private val context = context.applicationContext
        private var defaultPrefsName = context.applicationContext.packageName
        private var password: String? = null
        private var salt: ByteArray? = null
        private var defaultPrefsMode = Context.MODE_PRIVATE
        private var cacheEnabled = true
        private var logLevel = LOG_DISABLED

        /**
         * Set the custom obfuscator that will be used to obfuscate and deobfuscate values inside SharedPreferences.
         * Passing null will not obfuscate data.
         * @param obfuscator Interface that will be used when putting and getting data from SharedPreferences
         */
        @Deprecated(CRYPTER_DEPRECATION_MESSAGE, replaceWith = ReplaceWith("setObfuscator"))
        fun setCrypter(obfuscator: Crypter): Builder = setObfuscator(CrypterToObfuscator(obfuscator))

        /**
         * Set the custom obfuscator that will be used to obfuscate and deobfuscate values inside SharedPreferences.
         * Passing null will not obfuscate data.
         * @param obfuscator Interface that will be used when putting and getting data from SharedPreferences
         */
        fun setObfuscator(obfuscator: Obfuscator): Builder {
            this.obfuscator = obfuscator
            return this
        }

        /**
         * Use the provided [pass] and [salt] with default obfuscator to obfuscate/deobfuscate values inside
         * SharedPreferences. The default obfuscator uses AES algorithm and then encode/decode data in base64.
         * If [salt] is null, it will be automatically generated using SecureRandom, obfuscated using the password and
         *  saved in the same sharedPreferences file, without possibility of conflicts with current (or future) keys.
         */
        @Deprecated(CRYPTER_DEPRECATION_MESSAGE, replaceWith = ReplaceWith("setObfuscator"))
        fun setCrypter(pass: String, salt: ByteArray?): Builder = setObfuscator(pass, salt)

        /**
         * Use the provided [pass] and [salt] with default obfuscator to obfuscate/deobfuscate values inside
         * SharedPreferences. The default obfuscator uses AES algorithm and then encode/decode data in base64.
         * If [salt] is null, it will be automatically generated using SecureRandom, obfuscated using the password and
         *  saved in the same sharedPreferences file, without possibility of conflicts with current (or future) keys.
         */
        fun setObfuscator(pass: String, salt: ByteArray?): Builder {
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
         * @param useObfuscator If true, it will use the same obfuscator of defaultPreferences, if specified
         * If false, this file will not be obfuscated
         */
        fun addPrefs(prefsName: String, mode: Int, useObfuscator: Boolean): Builder {
            prefMap[prefsName] = PrefContainer(useObfuscator, prefsName, mode)
            return this
        }

        /** Set the [logLevel] application-wide as one of [Prefs.Builder].LOG_... values. */
        fun setLogLevel(logLevel: Int): Builder {
            this.logLevel = logLevel
            return this
        }

        /**
         * Set the function [onPrefSet] to call when a preference is put into the shared preferences.
         * If no obfuscator is in use, obfuscated key and value will be the same of key and value.
         */
        fun setOnPreferenceSet(
            onPrefSet: (
                pref: PowerfulPreference<*>?,
                key: String,
                value: String,
                obfuscatedKey: String,
                obfuscatedValue: String,
                preferenceFileName: String
            ) -> Unit
        ): Builder {
            onPreferenceSet = onPrefSet
            return this
        }

        /** Initializes the library with previous provided configuration. */
        fun build() {
            prefContainerBuilder = { prefContainer -> prefContainer.build(context) }
            mDefaultPrefs = context.applicationContext.getSharedPreferences(defaultPrefsName, defaultPrefsMode)
            mDefaultName = defaultPrefsName
            mCacheEnabled = cacheEnabled

            // If the user set a password, I generate the default obfuscator and use it
            mObfuscator = this.obfuscator
            if (password?.isNotEmpty() == true) {
                mObfuscator = generateDefaultObfuscator(mDefaultPrefs, password!!, salt)
            }

            val defaultPrefContainer = prefMap[defaultPrefsName]
                ?: PrefContainer(mObfuscator != null, defaultPrefsName, defaultPrefsMode)
            powerfulPrefMap.clear()
            logger.setLevel(logLevel)
            logger.logV("Initialized with default SharedPreferences $defaultPrefsName")

            prefMap[defaultPrefsName] = defaultPrefContainer
            preInitPrefFileNames.filter { !prefMap.containsKey(it) }.forEach {
                val useObfuscator = mObfuscator != null
                prefMap[it] = PrefContainer(useObfuscator, it, Context.MODE_PRIVATE)
                logger.logE(
                    "The preference file $it has not been added during init. It's going to be used " +
                        "with obfuscation=$useObfuscator. Please specify $it explicitly during init like so:" +
                        "Prefs.init().setObfuscator(...).addPrefs(\"$it\", Context.MODE_PRIVATE, $useObfuscator)"
                )
            }

            prefMap.values.forEach {
                it.build(context)
                logger.logV(
                    "SharedPreferences files: ${it.name} (obfuscation: ${mObfuscator != null && it.useObfuscator})"
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

            /** Logs everything, from errors (even obfuscating/deobfuscating), to values and keys, to other methods. */
            const val LOG_VERBOSE = 3
        }
    }

    private fun generateDefaultObfuscator(prefs: SharedPreferences, pass: String, saltPassed: ByteArray?): Obfuscator {
        var salt = saltPassed
        if (salt == null) {
            var obfuscatedSalt: String? = ""
            try {
                val c = DefaultObfuscator(pass, pass.toByteArray())
                obfuscatedSalt = prefs.getString(c.obfuscate("key") + "!", "")

                if (obfuscatedSalt.isNullOrEmpty()) {
                    obfuscatedSalt = SecureRandom().nextLong().toString() + ""
                    logger.logD("Creating a new salt for the default obfuscator: $obfuscatedSalt")
                    val obfuscatedSaltKey = c.obfuscate("key") + "!"
                    val obfuscatedSaltValue = c.obfuscate(obfuscatedSalt)
                    prefs.edit().putString(obfuscatedSaltKey, obfuscatedSaltValue).apply()
                } else {
                    obfuscatedSalt = c.deobfuscate(obfuscatedSalt)
                }

                salt = obfuscatedSalt.toByteArray(charset(CHARSET_UTF8))
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Salt generation error for $obfuscatedSalt: ", e)
            }
        }
        return DefaultObfuscator(pass, salt)
    }

    /**
     * Changes the values of all the preferences files, obfuscating them with a new [pass] and [salt].
     * If [salt] is null, it will be automatically generated using SecureRandom, obfuscated using the password and then
     * saved in the same sharedPreferences file, without possibility of conflicts with current (or future) keys.
     * Only preferences set up to use an obfuscator will be changed.
     * All values are changed in a transaction: if an error occurs, none of them will be changed.
     *
     * Use the provided obfuscator that will be used to obfuscate and deobfuscate values inside SharedPreferences.
     * The provided obfuscator uses AES algorithm and then encode/decode data in base64.
     */
    @Deprecated(CRYPTER_DEPRECATION_MESSAGE, replaceWith = ReplaceWith("changeObfuscator"))
    fun changeCrypter(pass: String, salt: ByteArray?) = changeObfuscator(pass, salt)

    /**
     * Changes the values of all the preferences files, obfuscating them with a new [pass] and [salt].
     * If [salt] is null, it will be automatically generated using SecureRandom, obfuscated using the password and then
     * saved in the same sharedPreferences file, without possibility of conflicts with current (or future) keys.
     * Only preferences set up to use an obfuscator will be changed.
     * All values are changed in a transaction: if an error occurs, none of them will be changed.
     *
     * Use the provided obfuscator that will be used to obfuscate and deobfuscate values inside SharedPreferences.
     * The provided obfuscator uses AES algorithm and then encode/decode data in base64.
     */
    fun changeObfuscator(pass: String, salt: ByteArray?) {
        val newObfuscator = generateDefaultObfuscator(mDefaultPrefs, pass, salt)
        changeObfuscator(newObfuscator)
    }

    /**
     * Changes the values of all the preferences files, obfuscating them with the [newObfuscator].
     * If [newObfuscator] is null, the current obfuscator will be removed, and values will be deobfuscated.
     * Only preferences already set up to use an obfuscator will be changed.
     * All values are changed in a transaction: if an error occurs, none of them will change.
     */
    @Deprecated(CRYPTER_DEPRECATION_MESSAGE, replaceWith = ReplaceWith("changeObfuscator"))
    @Synchronized
    fun changeCrypter(newObfuscator: Crypter?) =
        changeObfuscator(newObfuscator?.let { CrypterToObfuscator(it) })

    /**
     * Changes the values of all the preferences files, obfuscating them with the [newObfuscator].
     * If [newObfuscator] is null, the current obfuscator will be removed, and values will be deobfuscated.
     * Only preferences already set up to use an obfuscator will be changed.
     * All values are changed in a transaction: if an error occurs, none of them will change.
     */
    @Synchronized
    fun changeObfuscator(newObfuscator: Obfuscator?) {
        val maps = HashMap<String, Map<String, Triple<String, String, String>>>(prefMap.size)
        if (mCacheEnabled) cacheMap.clear()

        // I update the obfuscator only for preferences already using an obfuscator
        prefMap.values.filter { it.useObfuscator }.forEach { prefContainer ->
            val values = prefContainer.sharedPreferences?.all ?: return
            val newValues = HashMap<String, Triple<String, String, String>>(values.size)

            // Populate newValues map with old key/values obfuscated with the new obfuscator
            try {
                values.keys.forEach {
                    val newKey = mObfuscator?.deobfuscate(it) ?: it
                    val newVal =
                        mObfuscator?.deobfuscate(values[it].toString())?.substringBefore(it) ?: values[it].toString()
                    newValues[newObfuscator?.obfuscate(newKey) ?: newKey] =
                        Triple(newKey, newVal, newObfuscator?.obfuscate(newVal) ?: newVal)
                }
            } catch (e: IllegalArgumentException) {
                logger.logE(
                    "Trying to change obfuscator, but got an error:\n${e.localizedMessage}\nNo values were changed!"
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
                val newDeobfuscatedKey = newValue.first
                val newDeobfuscatedValue = newValue.second
                val newObfuscatedValue = newValue.third

                prefContainer.sharedPreferences?.edit()?.putString(newKey, newObfuscatedValue)?.apply()
                onPreferenceSet?.invoke(
                    null,
                    newDeobfuscatedKey,
                    newDeobfuscatedValue,
                    newKey,
                    newObfuscatedValue,
                    prefContainer.name
                )
            }
        }

        mObfuscator = newObfuscator
        logger.logV("Obfuscator was changed, and all values have been obfuscated")
    }

    internal fun registerPreference(preference: PowerfulPreference<Any?>) {
        val ps = powerfulPrefMap[preference.key] ?: ArrayList()
        ps.add(WeakReference(preference))
        powerfulPrefMap[preference.key] = ps

        preference.preferencesFileName?.let {
            if (prefMap.isNotEmpty() && !prefMap.containsKey(it)) {
                val useObfuscator = mObfuscator != null
                val container = PrefContainer(useObfuscator, it, Context.MODE_PRIVATE)
                prefMap[it] = container
                logger.logE(
                    "The preference file $it has not been added during init. It's going to be used " +
                        "with obfuscation=$useObfuscator. Please specify $it explicitly during init like so:" +
                        "Prefs.init().setObfuscator(...).addPrefs(\"$it\", Context.MODE_PRIVATE, $useObfuscator)"
                )
                prefContainerBuilder.invoke(container)
            } else {
                preInitPrefFileNames.add(it)
            }
        }
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
     * For preferences with nullable values, use [newNullablePref].
     *
     * Note: The return type is inferred from the value type.
     */
    fun <T> newPref(key: String, value: T, prefName: String? = null): PowerfulPreference<T> {
        val preference: PowerfulPreference<T> = when (value) {
            is Int -> IPreference(key, value as Int, prefName)
            is Float -> FPreference(key, value as Float, prefName)
            is Double -> DPreference(key, value as Double, prefName)
            is Boolean -> BPreference(key, value as Boolean, prefName)
            is String -> SPreference(key, value as String, prefName)
            is Long -> LPreference(key, value as Long, prefName)
            else -> null
        } as? PowerfulPreference<T>
            ?: throw IllegalArgumentException("Cannot understand preference type $value. Please, provide a valid class")

        logger.logV("Created preference $key : ${preference.toPreferences(value)} (${preference.getClassName()})")
        return preference
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with primitive types (and their boxed types) like int, Integer, boolean, Boolean...
     * For technical reasons, the String type cannot be used as nullable preference.
     *
     * It takes the [key] of the preference, the default [value] to return in case of errors and the file [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     * For Enums, use [newEnumPref].
     * For preferences without nullable values, use [newPref].
     *
     * Note: The return type cannot be inferred, so must be specified through [clazz].
     */
    fun <T : Any> newNullablePref(
        clazz: Class<T>,
        key: String,
        value: T?,
        prefName: String? = null
    ): PowerfulPreference<T?> {
        val pref: PowerfulPreference<T?> = when {
            clazz.isAssignableFrom(Int::class.java) -> IPreferenceNullable(key, value as? Int, prefName)
            clazz.isAssignableFrom(Float::class.java) -> FPreferenceNullable(key, value as? Float, prefName)
            clazz.isAssignableFrom(Double::class.java) -> DPreferenceNullable(key, value as? Double, prefName)
            clazz.isAssignableFrom(Boolean::class.java) -> BPreferenceNullable(key, value as? Boolean, prefName)
            clazz.isAssignableFrom(Long::class.java) -> LPreferenceNullable(key, value as? Long, prefName)
            clazz.isAssignableFrom(String::class.java) -> throw IllegalArgumentException(
                "Cannot use String as a nullable preference. " +
                    "Please, provide another class or specify the parse and toPreference functions."
            )
            else -> null
        } as? PowerfulPreference<T?> ?: throw IllegalArgumentException(
            "Cannot understand preference type ${clazz.name}. Please, provide a valid class"
        )

        logger.logV("Created nullable preference $key : ${pref.toPreferences(value)} (${pref.getClassName()})")
        return pref
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works with any object, as long as [parse] and [toPreference] are provided.
     * It takes the [key] of the preference, the default [value] to return in case of errors and the file [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     * If the [toPreference] is null, the [toString] method will be used.
     * For Enums, use [newEnumPref]
     * For preferences with nullable values, use [newNullablePref].
     *
     * Note: The return type is inferred from the value type
     */
    fun <T : Any> newPref(
        key: String,
        value: T,
        prefName: String? = null,
        parse: (s: String) -> T,
        toPreference: (t: T) -> String = { it.toString() }
    ): PowerfulPreference<T> {
        val preference: PowerfulPreference<T> = ObjPreference(key, value, prefName, parse, toPreference)
        logger.logV("Created preference $key : ${preference.toPreferences(value)} (${preference.getClassName()})")
        return preference
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works with any object, as long as [parse] and [toPreference] are provided.
     * It takes the [key] of the preference, the default [value] to return in case of errors and the file [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     * If the [toPreference] is null, the [toString] method will be used, using an empty string if the value is null.
     * For Enums, use [newEnumPref]
     * For preferences without nullable values, use [newPref].
     *
     * Note: The return type cannot be inferred, so must be specified through [clazz].
     */
    @Suppress("LongParameterList")
    fun <T> newNullablePref(
        clazz: Class<T>,
        key: String,
        value: T?,
        prefName: String? = null,
        parse: (s: String) -> T?,
        toPreference: (t: T?) -> String = { it?.toString() ?: "" }
    ): PowerfulPreference<T?> {
        val pref: PowerfulPreference<T?> = ObjPreferenceNullable(clazz, key, value, prefName, parse, toPreference)
        parse("")?.let { it::class.java }
        logger.logV("Created nullable preference $key : ${pref.toPreferences(value)} (${pref.getClassName()})")
        return pref
    }

    /**
     * Convenience method to easily create PowerfulPreferences.
     * This works only with Enums
     * It takes the [key] of the preference, the default [value] to return in case of errors and the file [prefName].
     * If the [prefName] is null, the default preferences file will be used.
     *
     * Note: For other types refer to [newPref]
     */
    fun <T : Enum<T>> newEnumPref(key: String, value: T, prefName: String? = null):
        PowerfulPreference<T> {
        val preference: PowerfulPreference<T> = EnumPreference(key, value, prefName)
        logger.logV("Created enum preference $key : ${preference.toPreferences(value)} (${preference.getClassName()})")
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
            logger.logD("Retrieved from cache ${pref.key} : ${pref.toPreferences(value)} (${pref.getClassName()})")
            return value
        }

        val value = getAndDeobfuscate(pref.key, pref.preferencesFileName)
        val valueToReturn: T

        if (value.isEmpty()) {
            logger.logE(
                "No data found for key ${pref.key}. Returning default value: ${pref.toPreferences(pref.defaultValue)}"
            )
            valueToReturn = pref.defaultValue
        } else {
            valueToReturn = try {
                val parsed = pref.parse(value)
                logger.logD("Retrieved ${pref.key} : $value (${pref.getClassName()})")
                parsed
            } catch (e: NumberFormatException) {
                logger.logE(
                    "Error trying to parse ${pref.key} : $value as ${pref.getClassName()}." +
                        "\n${e.localizedMessage}\nReturning default value: ${pref.toPreferences(pref.defaultValue)}"
                )
                pref.defaultValue
            } catch (ignored: Exception) {
                logger.logE(
                    "Don't know how to parse ${pref.key} : '$value' as ${pref.getClassName()}. " +
                        "Returning default value: ${pref.toPreferences(pref.defaultValue)}"
                )
                pref.defaultValue
            }
        }
        if (mCacheEnabled && valueToReturn != pref.defaultValue && pref !is DummyPreference) {
            cacheMap[pref.getCacheMapKey()] = valueToReturn
        }
        return valueToReturn
    }

    /**
     * Stores the [value] into the preference with specified [key] and [preferenceName].
     * If [preferenceName] is null, the default preferences file is used.
     * Its [PowerfulPreference.toPreferences] method will be called to get the string to save
     */
    @Synchronized
    fun <T> put(key: String, value: T, preferenceName: String? = null) {
        val pref = DummyPreference(key, value, preferenceName)
        cacheMap.remove(pref.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(key, value as Any) }
        logger.logD("Put ${pref.key} : ${pref.toPreferences(value?.toString() ?: "")} (${pref.getClassName()})")
        obfuscateAndPut(pref, pref.key, pref.toPreferences(value?.toString() ?: ""), pref.preferencesFileName)

        powerfulPrefMap[key]
            ?.filter { it.get()?.preferencesFileName == preferenceName }
            ?.forEach {
                it.get()?.callOnChange()
            }
    }

    /** Stores the [value] into the [pref]. Its toPreferences() method will be called to get the string to save */
    @Synchronized
    fun <T> put(pref: PowerfulPreference<T>, value: T) {
        if (mCacheEnabled && value != pref.defaultValue) cacheMap[pref.getCacheMapKey()] = value
        prefChangedCallbacks.forEach { it.invoke(pref.key, value as Any) }
        powerfulPrefMap[pref.key]
            ?.filter { it.get()?.preferencesFileName == pref.preferencesFileName }
            ?.forEach {
                it.get()?.callOnChange(value)
            }
        logger.logD("Put ${pref.key} : ${pref.toPreferences(value)} (${pref.getClassName()})")
        obfuscateAndPut(pref, pref.key, pref.toPreferences(value), pref.preferencesFileName)
    }

    /**
     * Remove the preference with specified [key] and [preferenceName]. The callbacks will receive an empty string.
     * If [preferenceName] is null, the default preferences file is used.
     */
    @Synchronized
    fun remove(key: String, preferenceName: String? = null) =
        remove(DummyPreference(key, null, preferenceName))

    /** Remove the [preference]. The callback will receive the default value of the preference. */
    @Synchronized
    fun <T> remove(preference: PowerfulPreference<T>) {
        if (mCacheEnabled) cacheMap.remove(preference.getCacheMapKey())
        prefChangedCallbacks.forEach { it.invoke(preference.key, preference.defaultValue as Any) }

        powerfulPrefMap[preference.key]
            ?.filter { it.get()?.preferencesFileName == preference.preferencesFileName }
            ?.forEach { it.get()?.callOnChange(preference.defaultValue) }
        val editor = findPref(preference.preferencesFileName).edit()

        if (findObfuscator(preference.preferencesFileName) == null) {
            editor?.remove(preference.key)
        }

        editor?.remove(
            tryOr(preference.key) { findObfuscator(preference.preferencesFileName)?.obfuscate(preference.key) }
        )
        editor?.apply()
    }

    /**
     * Returns whether the preference with the passed [key] exists in [preferenceName] file.
     * If [preferenceName] is null, the default preferences file is used.
     */
    @Synchronized
    fun contains(key: String, preferenceName: String? = null): Boolean =
        contains(DummyPreference(key, null, preferenceName))

    /** Returns whether the passed [preference] exists. */
    @Synchronized
    operator fun <T> contains(preference: PowerfulPreference<T>): Boolean {
        val sharedPreferences = findPref(preference.preferencesFileName)
        val found: Boolean

        if (findObfuscator(preference.preferencesFileName) == null) {
            found = sharedPreferences.contains(preference.key)
            logger.logD("Check existence of ${preference.key}: $found")
            return found
        }

        found = sharedPreferences.contains(
            tryOr(preference.key) { findObfuscator(preference.preferencesFileName)?.obfuscate(preference.key) }
        )

        logger.logD("Check existence of ${preference.key}: $found")
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

        logger.logD("Clearing all the preferences")
        val editor = findPref(preferencesFileName).edit()
        editor.clear().commit()
        if (preferencesFileName == null) cacheMap.clear()
        return editor
    }

    /** Returns the file names of all the preferences used, including the default one. */
    @Synchronized
    fun getAllPreferenceFileNames() = prefMap.values.map { it.name }

    @TestOnly
    internal fun clearForTests() {
        getAllPreferenceFileNames().forEach { clear(it) }
        mObfuscator = null
        powerfulPrefMap.clear()
        prefMap.clear()
        cacheMap.clear()
        prefChangedCallbacks.clear()
        preInitPrefFileNames.clear()
    }

    /**
     * @return Returns a map containing all obfuscated preferences.
     * If the [preferencesFileName] is null, the default preferences file will be used.
     * Note:
     *  plain preferences will be returned for unobfuscated preference files.
     *  Also, the value of the obfuscated preference is the obfuscation of the value and the obfuscated key.
     *  In other words: value = obfuscator.obfuscate(value + obfuscatedKey)
     * @see android.content.SharedPreferences.getAll
     */
    @Deprecated(CRYPTER_DEPRECATION_MESSAGE, replaceWith = ReplaceWith("getAllObfuscated"))
    fun getAllEncrypted(preferencesFileName: String? = null) = getAllObfuscated(preferencesFileName)

    /**
     * @return Returns a map containing all obfuscated preferences.
     * If the [preferencesFileName] is null, the default preferences file will be used.
     * Note:
     *  plain preferences will be returned for unobfuscated preference files.
     *  Also, the value of the obfuscated preference is the obfuscation of the value and the obfuscated key.
     *  In other words: value = obfuscator.obfuscate(value + obfuscatedKey)
     * @see android.content.SharedPreferences.getAll
     */
    fun getAllObfuscated(preferencesFileName: String? = null): Map<String, *> {
        logger.logD("Retrieving all the preferences")
        return findPref(preferencesFileName).all ?: HashMap<String, Any>()
    }

    /**
     * @return Returns a map containing all deobfuscated preferences of the specified preference file.
     * If the [preferencesFileName] is null, the default preferences file will be used.
     * @see android.content.SharedPreferences.getAll
     */
    fun getAll(preferencesFileName: String? = null): Map<String, *> {
        logger.logD("Retrieving all the preferences")
        return findPref(preferencesFileName).all
            ?.map { getObfuscatedAndDeobfuscate(it.key, preferencesFileName) }
            ?.toMap() ?: HashMap<String, Any>()
    }

    /** Deobfuscates the value corresponding to a key. */
    private fun getObfuscatedAndDeobfuscate(key: String, preferencesFileName: String?): Pair<String, String> {
        val sharedPreferences = findPref(preferencesFileName)

        return try {
            val obfuscator = findObfuscator(preferencesFileName)
            val obfuscatedValue = sharedPreferences.getString(key, "")

            when {
                obfuscator == null -> Pair(key, sharedPreferences.getString(key, "") ?: "")
                obfuscatedValue?.isEmpty() != false -> Pair(obfuscator.deobfuscate(key), "")
                else -> {
                    val deobfuscatedKey = obfuscator.deobfuscate(key)
                    val value = obfuscator.deobfuscate(obfuscatedValue).replace(key, "")
                    logger.logV("Retrieved $deobfuscatedKey : $value from $key : $obfuscatedValue")
                    Pair(deobfuscatedKey, value)
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.logE("Error deobfuscating $key \n${e.localizedMessage}")
            Pair(key, sharedPreferences.getString(key, "") ?: "")
        }
    }

    /** Deobfuscates the value corresponding to a key. */
    private fun getAndDeobfuscate(key: String, preferencesFileName: String?): String {
        val sharedPreferences = findPref(preferencesFileName)

        return try {
            val obfuscator = findObfuscator(preferencesFileName)
            val obfuscatedKey = obfuscator?.obfuscate(key)
            val obfuscatedValue = sharedPreferences.getString(obfuscatedKey ?: "", "")

            when {
                obfuscator == null || obfuscatedKey == null -> sharedPreferences.getString(key, "") ?: ""
                obfuscatedValue?.isEmpty() != false -> ""
                else -> {
                    val value = obfuscator.deobfuscate(obfuscatedValue).replace(obfuscatedKey, "")
                    logger.logV("Retrieved $key : $value from $obfuscatedKey : $obfuscatedValue")
                    value
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.logE("Error deobfuscating $key \n${e.localizedMessage}")
            sharedPreferences.getString(key, "") ?: ""
        }
    }

    /** Obfuscates a value. */
    private fun obfuscateAndPut(preference: PowerfulPreference<*>, key: String, value: String, prefFileName: String?) {
        val sharedPreferences = findPref(prefFileName)
        val editor = sharedPreferences.edit()
        val obfuscator = findObfuscator(prefFileName)
        if (obfuscator == null) {
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
            val obfuscatedKey = obfuscator.obfuscate(key)
            val obfuscatedValue = obfuscator.obfuscate(value.trim() + obfuscatedKey)

            editor.putString(obfuscatedKey, obfuscatedValue).apply()

            onPreferenceSet?.invoke(
                preference,
                key,
                value.trim(),
                obfuscatedKey,
                obfuscatedValue,
                prefFileName ?: mDefaultName
            )
            logger.logV("Saving $key : ${value.trim()} as $obfuscatedKey : $obfuscatedValue")
        } catch (e: IllegalArgumentException) {
            logger.logE("Error obfuscating $key : $value \n${e.localizedMessage}")
        }
    }

    private fun findPref(name: String?): SharedPreferences {
        if (name.isNullOrEmpty()) {
            return mDefaultPrefs
        }
        return prefMap[name]?.sharedPreferences ?: mDefaultPrefs
    }

    private fun findObfuscator(name: String?): Obfuscator? {
        if (name.isNullOrEmpty()) {
            return mObfuscator
        }
        return if (prefMap[name]?.useObfuscator == true) mObfuscator else null
    }
}

internal inline fun <T> tryOr(default: T, function: () -> T): T {
    return try {
        function.invoke()
    } catch (ignored: Exception) {
        default
    }
}
