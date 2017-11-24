package com.stefanosiano.powerfulsharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

/**
 * Created by stefano on 11/23/17.
 */

class PrefContainer {
    SharedPreferences sharedPreferences;
    boolean useCrypter;
    String name;
    int mode;

    PrefContainer(boolean useCrypter, String name, int mode) {
        this.useCrypter = useCrypter;
        this.name = name;
        this.mode = mode;
    }
    void build(Context context){
        sharedPreferences = context.getApplicationContext().getSharedPreferences(name, mode);
    }
}