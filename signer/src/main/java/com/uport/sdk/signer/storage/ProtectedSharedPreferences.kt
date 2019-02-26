@file:Suppress("TooManyFunctions")

package com.uport.sdk.signer.storage

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE

/**
 * Meant to be an encrypted drop in replacement for [SharedPreferences]
 *
 * This class is NOT thread safe
 */
class ProtectedSharedPreferences(
        context: Context,
        @VisibleForTesting(otherwise = PACKAGE_PRIVATE) val delegate: SharedPreferences
) : SharedPreferences {

    val crypto = CryptoUtil(context)

    init {
        //encrypt all previously nonencrypted data
        delegate.all.entries
                .filter { (k, v) -> k != null && v != null }
                .filter { (k, _) -> !k.matches(Regex("^[sbifle]:.*")) }
                .forEach { (k, v) ->
                    when (v) {
                        is String -> {
                            edit().putString(k, v).apply()
                            delegate.edit().remove(k).apply()
                        }
                        is Int -> {
                            edit().putInt(k, v).apply()
                            delegate.edit().remove(k).apply()
                        }
                        is Boolean -> {
                            edit().putBoolean(k, v).apply()
                            delegate.edit().remove(k).apply()
                        }
                        is Float -> {
                            edit().putFloat(k, v).apply()
                            delegate.edit().remove(k).apply()
                        }
                        is Long -> {
                            edit().putLong(k, v).apply()
                            delegate.edit().remove(k).apply()
                        }
                        is Set<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            edit().putStringSet(k, v as MutableSet<String>?).apply()
                            delegate.edit().remove(k).apply()
                        }
                    }
                }
    }

    override fun contains(key: String?): Boolean {
        if (key == null)
            return false

        return allKeyPrefixes
                .map { "$it:$key" }
                .fold(false) { foundIt, queryKey ->
                    foundIt or (delegate.contains(queryKey) and canDecrypt(queryKey))
                }
    }

    private fun canDecrypt(queryKey: String): Boolean {
        return try {
            if (queryKey.matches("^e:.*".toRegex())) {
                val set = delegate.getStringSet(queryKey, mutableSetOf(/*nothing*/)).orEmpty()
                set.forEach { crypto.decrypt(it) }
            } else {
                crypto.decrypt(delegate.getString(queryKey, "") ?: "")
            }
            true
        } catch (ex: Exception) {
            //"removing key: $queryKey because it can't be decrypted"
            delegate.edit().remove(queryKey).apply()
            false
        }
    }

    override fun getBoolean(key: String?, default: Boolean): Boolean {
        if (key == null)
            return default
        val queryKey = "b:$key"
        if (!delegate.contains(queryKey)) {
            return default
        }

        val decryptedBytes = try {
            crypto.decrypt(delegate.getString(queryKey, "") ?: "")
        } catch (ex: Exception) {
            delegate.edit().remove(queryKey).apply()
            return default
        }

        return String(decryptedBytes).toBoolean()
    }

    override fun getInt(key: String?, default: Int): Int {
        if (key == null)
            return default
        val queryKey = "i:$key"
        if (!delegate.contains(queryKey)) {
            return default
        }
        val decryptedBytes = try {
            crypto.decrypt(delegate.getString(queryKey, "") ?: "")
        } catch (ex: Exception) {
            delegate.edit().remove(queryKey).apply()
            return default
        }
        return String(decryptedBytes).toInt()
    }

    override fun getLong(key: String?, default: Long): Long {
        if (key == null)
            return default
        val queryKey = "l:$key"
        if (!delegate.contains(queryKey)) {
            return default
        }

        val decryptedBytes = try {
            crypto.decrypt(delegate.getString(queryKey, "") ?: "")
        } catch (ex: Exception) {
            delegate.edit().remove(queryKey).apply()
            return default
        }
        return String(decryptedBytes).toLong()
    }

    override fun getFloat(key: String?, default: Float): Float {
        if (key == null)
            return default
        val queryKey = "f:$key"
        if (!delegate.contains(queryKey)) {
            return default
        }

        val decryptedBytes = try {
            crypto.decrypt(delegate.getString(queryKey, "") ?: "")
        } catch (ex: Exception) {
            delegate.edit().remove(queryKey).apply()
            return default
        }
        return String(decryptedBytes).toFloat()
    }

    override fun getString(key: String?, default: String?): String? {
        if (key == null)
            return default
        val queryKey = "s:$key"
        if (!delegate.contains(queryKey)) {
            return default
        }

        val decryptedBytes = try {
            crypto.decrypt(delegate.getString(queryKey, "") ?: "")
        } catch (ex: Exception) {
            delegate.edit().remove(queryKey).apply()
            return default
        }
        return String(decryptedBytes)
    }

    override fun getStringSet(key: String?, default: MutableSet<String>?): MutableSet<String>? {
        if (key == null)
            return default
        val queryKey = "e:$key"
        if (!delegate.contains(queryKey)) {
            return default
        }

        val encryptedValues = delegate.getStringSet(queryKey, HashSet<String>()) ?: return default

        return try {
            encryptedValues
                    .map { crypto.decrypt(it) }
                    .map { String(it) }
                    .toMutableSet()
        } catch (ex: Exception) {
            delegate.edit().remove(queryKey).apply()
            default
        }
    }

    override fun edit(): SharedPreferences.Editor {
        return EncryptedEditor(delegate.edit(), crypto)
    }

    override fun getAll(): MutableMap<String?, Any?> {
        return delegate.all.entries
                .filter { (k, v) -> k != null && v != null }
                .map { (key, _) ->

                    //assumes all key prefixes are length 2
                    val queryKey = key.substring(2)

                    val value: Any? = when {
                        Regex("^s:.*").matches(key) -> getString(queryKey, null)
                        Regex("^b:.*").matches(key) -> getBoolean(queryKey, false)
                        Regex("^i:.*").matches(key) -> getInt(queryKey, 0)
                        Regex("^f:.*").matches(key) -> getFloat(queryKey, 0f)
                        Regex("^l:.*").matches(key) -> getLong(queryKey, 0L)
                        Regex("^e:.*").matches(key) -> getStringSet(queryKey, null)
                        else -> {
                            null
                        }
                    }

                    //this assume all key prefixes are length 2 ("s:")
                    Pair(queryKey, value)
                }
                .toMap()
                .toMutableMap()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        //todo: listener will be called with wrong keys; they need to be decrypted before calling
        delegate.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        //todo: listener will be called with wrong keys; they need to be decrypted before calling
        delegate.unregisterOnSharedPreferenceChangeListener(listener)
    }

    class EncryptedEditor(private var delegate: SharedPreferences.Editor, val crypto: CryptoUtil) : SharedPreferences.Editor {

        override fun clear(): SharedPreferences.Editor {
            delegate.clear()
            return this
        }

        private fun putPrimitive(key: String?, value: Any?, typePrefix: String = "s"): SharedPreferences.Editor {
            if (key == null)
                return this

            val encryptedValue = crypto.encrypt(value.toString().toByteArray())
            delegate.putString("$typePrefix:$key", encryptedValue)
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            return putPrimitive(key, value, "l")
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            return putPrimitive(key, value, "i")
        }

        override fun remove(key: String?): SharedPreferences.Editor {

            allKeyPrefixes
                    .map { "$it:$key" }
                    .forEach {
                        delegate.remove(it)
                    }

            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            return putPrimitive(key, value, "b")
        }

        override fun putStringSet(key: String?, valueSet: MutableSet<String>?): SharedPreferences.Editor {
            if (key == null || valueSet == null)
                return this
            val queryKey = "e:$key"

            val encryptedValues = valueSet
                    .map { crypto.encrypt(it.toByteArray()) }
                    .toMutableSet()

            return delegate.putStringSet(queryKey, encryptedValues)
        }

        override fun commit(): Boolean {
            return delegate.commit()
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            return putPrimitive(key, value, "f")
        }

        override fun apply() {
            delegate.apply()
        }

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            return putPrimitive(key, value, "s")
        }

    }

    companion object {

        //these prefixes, followed by a `:` signal the presence of an encrypted value.
        //there is a lot of space for collision with such short prefixes
        //TODO: use longer prefixes
        internal val allKeyPrefixes = listOf("b", "s", "i", "l", "f", "e")

    }

}
