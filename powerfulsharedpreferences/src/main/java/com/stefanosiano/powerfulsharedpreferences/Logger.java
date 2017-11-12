package com.stefanosiano.powerfulsharedpreferences;

import android.util.Log;

/** Class that will log everything, based on set log level. */

class Logger {

    private static int mLogLevel = 0;
    private static String mTag = "Prefs";

    static void setLevel(int logLevel, String tag){mLogLevel = logLevel; mTag = tag;}

    static void logGet(String key, String value, Class classs){
        if(mLogLevel < Prefs.Builder.LOG_VALUES)
            return;
        Log.i(mTag, "Retrieved " + classs.getSimpleName() + " " + key + " : " + value + ".");
    }
    static void logBuild(){}
    static void logGetAll(){}
    static void logRemove(String key){}
    static void logContains(String key){}
    static void logClear(){}
    static void logDecryptException(Exception e, String key){}
    static void logEncryptException(Exception e, String key){}
    static void logDecrypt(String key, String encryptedKey, String encryptedValue, String value){}
    static void logEncrypt(String key, String encryptedKey, String encryptedValue, String value){}

    static void logTerminate(){}

    static void logParseNotFound(String key, String defaultValue){}

    static void logParseNumberException(NumberFormatException e, String key, String defaultValue){}

    static void logParseTypeException(String key, String defaultValue){
        //Couldn't find correct value type for key
    }

    static void logPut(String key, String value){

    }
}
