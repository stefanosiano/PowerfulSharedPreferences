package com.stefanosiano.powerful_libraries.sharedpreferences_livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.stefanosiano.powerful_libraries.sharedpreferences.PowerfulPreference


class PowerfulPreferenceLiveData<T>(private val preference: PowerfulPreference<T>): LiveData<T>() {
    private val callback = { value: T -> postValue(value) }
    override fun onInactive() { super.onInactive(); preference.stopObserve(callback) }
    override fun onActive() { super.onActive(); preference.observe(callback) }
}
fun <T> PowerfulPreference<T>.asLiveData(): LiveData<T> = PowerfulPreferenceLiveData(this)
fun <T> PowerfulPreference<T>.observe(owner: LifecycleOwner, observer: Observer<T>) = asLiveData().observe(owner, observer)
