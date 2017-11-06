PowerfulSharedPreferences
=========================
Powerful and easy SharedPreferences wrapper, with support for automatic encryption
  
  
Usage
-----
  
Just initialize this library inside the onCreate() method of your Application class  
  
```
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Prefs class
        Prefs.init(this, "shared_prefs_file_name", Context.MODE_PRIVATE);
        // Initialize password and salt for encryption
        Prefs.setDefaultCrypter("password", null);
    }
}  
```
  
After that, you can simply put data through:

```
    Prefs.putString("key", value);
    Prefs.putInt("key", value);
    Prefs.putLong("key", value);
    Prefs.putBoolean("key", value);
    Prefs.putDouble("key", value);
    Prefs.putFloat("key", value);
```
  
Finally, get data back through:  

```
    Prefs.getString("key", defaultValue);
    Prefs.getInt("key", defaultValue);
    Prefs.getLong("key", defaultValue);
    Prefs.getBoolean("key", defaultValue);
    Prefs.getDouble("key", defaultValue);
    Prefs.getFloat("key", defaultValue);
```
  
  
All values will be automatically encrypted and decrypted (if Prefs.setCrypter() or Prefs.setDefaultCrypter() is called after initialization).  
  
  
Through Prefs.setCrypter(crypter) method, you can provide your own encryption implementation, in order to have more control over it.  
If you just want an easy encryption method, just use Prefs.setDefaultCrypter(password, salt). Default crypter uses AES algorithm and then encode/decode data in base64.  
  
  
  
Gradle
------
Not working yet  
  
```
dependencies {
    compile 'com.stefanosiano:powerfulsharedpreferences:0.0.1' // Put this line into module's build.gradle
}
```
  
  
  
  
Proguard
--------
No steps are required, since configuration is already included.  
  
  
  
