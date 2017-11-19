PowerfulSharedPreferences
=========================
Powerful and easy SharedPreferences wrapper, with support for automatic encryption and logs.
  
  
Planned features (for now):  
* Support multiple sharedPreferences files, with optional encryption for each file
  
  
Usage
-----
  
Just initialize this library inside the onCreate() method and terminate it inside the onTerminate() method of your Application class  
  
```
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.init(this)
                .setLogLevel(BuildConfig.DEBUG ? Prefs.Builder.LOG_VERBOSE : Prefs.Builder.LOG_DISABLED)
                .setPrefsName("file_name", Context.MODE_PRIVATE)
                .setDefaultCrypter("password", null)
                .build();
    }

    @Override
    public void onTerminate() {
        Prefs.terminate();
        super.onTerminate();
    }
}  
```
  
After that, you can simply put and get data through:

```
    Prefs.put("key", value);
    Prefs.get("key", defaultValue);
```
  
Type of data will be inferred from the given value type  
  
  
  
Suggested (Opinionated) Usage
-----------------------------
  
Instead of having a class with multiple declared constants, representing the keys of the preferences, you can declare the PowerfulPreferences<> objects, like this:

```
    public static final PowerfulPreference<Integer> preference1 = Prefs.newPref("key", 0);
    public static final PowerfulPreference<Double> preference2 = Prefs.newPref("key2", 0D);
```
  
This way you declare default values only once, along with their classes, to have type safety, too. For example, if you try to put a String as a value for a preference declared as Integer, compiler will get angry!  
To put and get values you can then:  

```
    Prefs.put(preference1, value);
    Prefs.get(preference1);
```
    
  
  
Encryption
----------
  
Through Prefs.setDefaultCrypter(crypter) method, you can provide your own encryption implementation, in order to have more control over it.  
If you just want an easy encryption method, just use Prefs.setDefaultCrypter(password, salt). Default crypter uses AES algorithm and then encodes/decodes data in base64. If passed salt is null, a salt will be automatically generated, using SecureRandom, and then will be saved inside the preference file itself (after being encypted with the given password). It will be saved with a key ending with an exclamation mark, since it's not in the Base64 charset, ensuring its key will always be unique.  
  
Both the keys and the values will be encrypted.  
  
Through Prefs.changeCrypter() you can change the encryption of the SharedPreferences file, decrypting values with previous crypter/password and encrypting them again with a new one. Passing null will remove encryption on a file.  
  
  
  
  
Gradle
------
  
```
dependencies {
    compile 'com.stefanosiano:powerfulsharedpreferences:0.1.0' // Put this line into module's build.gradle
}
```
  
  
Proguard
--------
No steps are required, since configuration is already included.  
  
  
  
Roadmap
-------
Add support for multiple sharedPreferences  

