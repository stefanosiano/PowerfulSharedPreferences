package com.stefanosiano.powerfulsharedpreferences;

import android.util.Log;

/** Class that will log everything, based on set log level. */

class Logger {

    private static int mLogLevel = 0;
    private static String mTag = "Prefs";

    static void setLevel(int logLevel, String tag){mLogLevel = logLevel; mTag = tag;}

    static void logBuild(){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        //todo Log.v(mTag, "Retrieved " + classs.getSimpleName() + " " + key + " : " + value + ".");
    }

    static void logTerminate(){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.d(mTag, "Terminating and releasing all objects in memory");
    }

    static void logGetAll(){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.d(mTag, "Retrieving all the preferences");
    }

    static void logClear(){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.d(mTag, "Clearing all the preferences");
    }

    static void logRemove(String key){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.d(mTag, "Removing " + key);
    }

    static void logGet(String key, String value, Class classs){
        if(mLogLevel < Prefs.Builder.LOG_VALUES)
            return;
        Log.d(mTag, "Retrieved " + classs.getSimpleName() + " " + key + " : " + value);
    }

    static void logPut(String key, String value, Class classs){
        if(mLogLevel < Prefs.Builder.LOG_VALUES)
            return;
        Log.d(mTag, "Put " + classs.getSimpleName() + " " + value + " into " + key);
    }

    static void logContains(String key, boolean found){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.d(mTag, "Check existance of " + key + ": " + found);
    }

    static void logDecryptException(Exception e, String key){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.e(mTag, "Error decrypting " + key + " \n" + e.getLocalizedMessage());
    }

    static void logEncryptException(Exception e, String key, String value){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.e(mTag, "Error encrypting " + key + " : " + value + " \n" + e.getLocalizedMessage());
    }

    static void logDecrypt(String key, String encryptedKey, String encryptedValue, String value){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.v(mTag, "Retrieved " + key + " : " + value + " from " + encryptedKey + " : " + encryptedValue);
    }

    static void logEncrypt(String key, String encryptedKey, String encryptedValue, String value){
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        Log.v(mTag, "Saving " + key + " : " + value + " as " + encryptedKey + " : " + encryptedValue);
    }

    static void logParseNotFound(String key, String defaultValue){
        if(mLogLevel < Prefs.Builder.LOG_ERRORS)
            return;
        Log.w(mTag, "No data found for key " + key + ". Returning default value: " + defaultValue);
    }

    static void logParseNumberException(NumberFormatException e, String key, String value, String defaultValue, Class classs){
        if(mLogLevel < Prefs.Builder.LOG_ERRORS)
            return;
        Log.e(mTag, "Error trying to parse " + key + " : " + value + " as " + classs.getSimpleName() + ". Returning default value: " + defaultValue);
        if(mLogLevel < Prefs.Builder.LOG_VERBOSE)
            return;
        e.printStackTrace();
    }

    static void logParseTypeException(String key, String value, String defaultValue, Class classs){
        if(mLogLevel < Prefs.Builder.LOG_ERRORS)
            return;
        Log.e(mTag, "Don't know hot to parse " + key + " : " + value + " as " + classs.getSimpleName() + ". Returning default value: " + defaultValue);
    }
}
