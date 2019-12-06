PowerfulSharedPreferences
=========================
Android library with a powerful and easy SharedPreferences wrapper, with support for obfuscation, logs, multiple SharedPreferences files, type safety and callback on changes (optionally with Android LiveData).  
  
  
This library focuses on:  
- Speed, with a non-obfuscated cache map  
- Ease of use, with logs and single items for wrapping methods, or possibility to use the preference as a delegated property  
- Reliability, with type safety  
- Simple safety, with obfuscation  
- Flexibility, with custom objects, support for enums, callbacks or live data on preferences changes and multiple preferences files  
  
  
Usage
-----
  
Just initialize this library inside the onCreate() method of your Application class  
  
```
class PermissionRulerApplication: Application() {
public class MyApplication extends Application {
    override fun onCreate() {
        super.onCreate()
        ...
        

        Prefs.init(this)
                .setLogLevel(if (BuildConfig.DEBUG) Prefs.Builder.LOG_VALUES else Prefs.Builder.LOG_DISABLED)
                .setDefaultPrefs("prefs", Context.MODE_PRIVATE)
                .setCrypter("PermissionRuler", null)
                .build()
    }
}  
```
  
  
Then, instead of having a class with multiple declared constants, representing the keys of the preferences, you can declare the PowerfulPreferences<> objects, like this:

```
    //PowerfulPreference instance: it provides all normal methods and observable ones  
    val preference1: PowerfulPreference<Boolean> = Prefs.newPref("key1", false)
    //Integer instance: it's a delegated property, but loses observable methods  
    val preference2: Integer by Prefs.newPref("key2", 0)
    val preference3: PowerfulPreference<MyEnym> = Prefs.newEnumPref("key3", MyEnum.Value1)
    val preference4: MyEnym by Prefs.newEnumPref("key4", MyEnum.Value2)
```
  
This way you declare default values only once, along with their classes, to have type safety, too. For example, if you try to put a String as a value for a preference declared as Integer, compiler will get angry!  
Notes:  
Enum classes are supported. They can be instantiated with the ```Prefs.newEnumPref()``` method, and under the hood they are saved as strings, using the ```Enum.name()``` method.  
When a preference is put, a String is saved in the file, so the method ```toString()``` will be called on the objects passed to the ```Prefs.put()``` method.  
  
To put and get values you can then:  

```
    Prefs.put("key", value)
    Prefs.get("key")
```
or even better:  

```
    val preference1: PowerfulPreference<Boolean> = Prefs.newPref("key1", false)
    preference1.put(value)
    preference1.get()
```
or you can use delegated properties, so that you can:    

```
    val preference1: Boolean by Prefs.newPref("key1", false)
    preference1 = value
    preference1
```
  
  
  
Finally, you can provide custom implementations of the preferences like:
  
```
  class BigDecimalPreference: PowerfulPreference<BigDecimal> {
      constructor(key: String, defaultValue: BigDecimal, prefName: String): super(key, defaultValue, prefName)
      constructor(key: String, defaultValue: BigDecimal): super(key, defaultValue)
      override fun getPrefClass(): Class<*> = BigDecimal::class.java
      override fun parse(s: String): BigDecimal = if( s.isEmpty() ) BigDecimal.ZERO else BigDecimal(s)
      override fun toPreferences(value: T): String = toString()
  }
```
  
or  
```
  class MyObject : PrefObj {
      override fun fromPreferences(s: String): PrefObj = Gson().fromJson(s, MyObject::class.java)
      override fun toPreferences(): String = Gson().toJson(this)
  }
  Prefs.newPref(MyObject::class.java, "preference1", MyObject())
```
  
  
Speed
-----
  
Shared preferences are already cached by Android, but what happens if you want to save a custom implementation of an object? You have to continuously marshal and unmarshal the value saved in the preferences to use the object. Also, if the shared preferences are obfuscated, the cache will use unobfuscated values, meaning it doesn't need to perform any operation to get them.  
Of course, if you don't need it, or you don't want unobfuscated values in memory, you can disable it through the ```disableCache()``` method of the ```Prefs.init()``` builder.  
  
  
  
  
Obfuscation
-----------
  
Basic safety is provided through obfuscation.  
*Note that whatever is on the client can be cracked, so don't put secrets inside the preferences, even with obfuscation.*  
  
Through Prefs.setDefaultCrypter(crypter) method, you can provide your own encryption implementation, in order to have more control over it.  
If you just want an easy encryption method, just use Prefs.setDefaultCrypter(password, salt). Default crypter uses AES algorithm and then encodes/decodes data in base64. If passed salt is null, a salt will be automatically generated, using SecureRandom, and then will be saved inside the preference file itself (after being encypted with the given password). It will be saved with a key ending with an exclamation mark, since it's not in the Base64 charset, ensuring its key will always be unique.  
  
Both the keys and the values will be encrypted. Also, the encrypted values will contain the encrypted key inside, to avoid having the same value associated to multiple keys.  
  
Through Prefs.changeCrypter() you can change the encryption of the SharedPreferences file, decrypting values with previous crypter/password and encrypting them again with a new one. Passing null will remove encryption on a file.  
  
  
  
  
Change callbacks
----------------
  
You can observe globally the changes of the sharedPreferences through ```Prefs.observe { key, value ->  }```.  
Alternatively, you can observe the changes of a single PowerfulPreference object through ```preference.observe { value -> }```.  
**Note that when you use these methods, you will have to call the stopObserve() method!**  
  
Finally, you can use the provided powerfulsharedpreferences_livedata module to use the preferences as LiveData, using ```preference.observe (LifecycleOwner, Observer{ value -> } )```.  
**When using the live data, you don't have to call the stopObserve() method**  
  
  
  
  
  
Gradle
------
  
```
dependencies {
    implementation 'com.stefanosiano.powerfullibraries:sharedpreferences:1.0.3' // Put this line into module's build.gradle
    implementation 'com.stefanosiano.powerfullibraries:sharedpreferences_livedata:1.0.1' // Put this line if you want to use a preference as a live data
}
```
  
  
Proguard
--------
No steps are required, since configuration is already included.  
  

