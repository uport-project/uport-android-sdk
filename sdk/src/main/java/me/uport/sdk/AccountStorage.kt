package me.uport.sdk

import android.content.SharedPreferences
import me.uport.sdk.identity.HDAccount

interface AccountStorage {
    fun upsert(newAcc: HDAccount)

    fun get(handle: String): HDAccount?

    fun delete(handle: String)

    fun all(): List<HDAccount>

    fun upsertAll(list: Collection<HDAccount>)
}


/**
 * An account storage mechanism that relies on [SharedPreferences] for persistence to disk
 *
 * Accounts are loaded during construction and then relayed from memory
 */
class SharedPrefsAccountStorage(
        private val prefs: SharedPreferences
) : AccountStorage {

    private val accounts = mapOf<String, HDAccount>().toMutableMap()

    init {
        prefs.getStringSet(KEY_ACCOUNTS, emptySet())
                .orEmpty()
                .forEach { serialized ->
                    val acc = try {
                        HDAccount.fromJson(serialized)
                    } catch (ex: Exception) {
                        null
                    }

                    acc?.let { upsert(it) }
                }
    }


    override fun upsert(newAcc: HDAccount) {
        accounts[newAcc.handle] = newAcc
        persist()
    }

    override fun upsertAll(list: Collection<HDAccount>) {
        list.forEach {
            accounts[it.handle] = it
        }

        persist()
    }

    override fun get(handle: String): HDAccount? = accounts[handle]

    override fun delete(handle: String) {
        accounts.remove(handle)
        persist()
    }

    override fun all(): List<HDAccount> = accounts.values.toList()

    private fun persist() {
        prefs.edit()
                .putStringSet(KEY_ACCOUNTS, accounts.values.map { it.toJson() }.toSet())
                .apply()
    }

    companion object {
        private const val KEY_ACCOUNTS = "accounts"
    }
}