package com.stefanosiano.powerfulsharedpreferences;

/** Class that will log everything, based on set log level. */

class Logger {

    static void logGet(String key, String value, Class classs){

    }
    static void logInit(){}
    static void logGetAll(){}
    static void logRemove(String key){}
    static void logContains(String key){}
    static void logClear(){}
    static void logDecryptException(Exception e, String key){}
    static void logEncryptException(Exception e, String key){}
    static void logDecrypt(String key, String encryptedKey, String encryptedValue, String value){}
    static void logEncrypt(String key, String encryptedKey, String encryptedValue, String value){}

    static void logDestroy(){}

    static void logParseNotFound(String key, String defaultValue){}

    static void logParseNumberException(NumberFormatException e, String key, String defaultValue){}

    static void logParseTypeException(String key, String defaultValue){
        //Couldn't find correct value type for key
    }

    static void logPut(String key, String value){

    }
}
