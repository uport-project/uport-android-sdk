package me.uport.sdk.fakes

import android.content.SharedPreferences

/**
 * fake sharedprefs usable during testing
 */
class InMemorySharedPrefs : SharedPreferences {

    private val mapOfValues = emptyMap<String, Any?>().toMutableMap()

    override fun contains(key: String?): Boolean = mapOfValues.containsKey(key)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            if (mapOfValues[key] == null || mapOfValues[key] !is Boolean) {
                defValue
            } else {
                mapOfValues[key] as Boolean
            }


    override fun getInt(key: String?, defValue: Int): Int =
            if (mapOfValues[key] == null || mapOfValues[key] !is Int) {
                defValue
            } else {
                mapOfValues[key] as Int
            }


    override fun getLong(key: String?, defValue: Long): Long =
            if (mapOfValues[key] == null || mapOfValues[key] !is Long) {
                defValue
            } else {
                mapOfValues[key] as Long
            }

    override fun getFloat(key: String?, defValue: Float): Float =
            if (mapOfValues[key] == null || mapOfValues[key] !is Float) {
                defValue
            } else {
                mapOfValues[key] as Float
            }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            if (mapOfValues[key] == null || mapOfValues[key] !is MutableSet<*>) {
                defValues
            } else {
                @Suppress("UNCHECKED_CAST")
                mapOfValues[key] as MutableSet<String>
            }


    override fun getString(key: String?, defValue: String?): String? =
            if (mapOfValues[key] == null || mapOfValues[key] !is String) {
                defValue
            } else {
                mapOfValues[key] as String
            }


    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): Map<String, *> = mapOfValues

    override fun edit(): SharedPreferences.Editor {

        return object : SharedPreferences.Editor {

            override fun clear(): SharedPreferences.Editor = apply {
                mapOfValues.clear()
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
                this@InMemorySharedPrefs.mapOfValues[key] = value
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
                this@InMemorySharedPrefs.mapOfValues[key] = value
            }

            override fun remove(key: String?): SharedPreferences.Editor = apply {
                mapOfValues.remove(key)
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
                mapOfValues[key] = value
            }

            override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = apply {
                mapOfValues[key] = values
            }

            override fun commit(): Boolean {
                return true
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
                mapOfValues[key] = value
            }

            override fun apply() {
                //nop
            }

            override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
                mapOfValues[key] = value
            }
        }

    }
}
