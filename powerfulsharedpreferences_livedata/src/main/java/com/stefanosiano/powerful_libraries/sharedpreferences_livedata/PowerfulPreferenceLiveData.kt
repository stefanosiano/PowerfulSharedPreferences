package com.stefanosiano.powerful_libraries.sharedpreferences_livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.stefanosiano.powerful_libraries.sharedpreferences.PowerfulPreference

/** LiveData which automatically starts/stops observing the preference for changes. */
class PowerfulPreferenceLiveData<T>(private val preference: PowerfulPreference<T>) : LiveData<T>() {
    init {
        value = preference.get()
    }
    private val callback = { value: T -> postValue(value) }
    override fun onInactive() { super.onInactive(); preference.stopObserve(callback) }
    override fun onActive() { super.onActive(); preference.observe(callback) }
}

/** Returns a [LiveData] observing the changes of the preference. */
fun <T> PowerfulPreference<T>.asLiveData(): LiveData<T> = PowerfulPreferenceLiveData(this)

/** Convenience method to observe the [LiveData] generated using the preference [asLiveData] method. */
fun <T> PowerfulPreference<T>.observe(owner: LifecycleOwner, observer: Observer<T>) =
    asLiveData().observe(owner, observer)
