package me.uport.sdk.fakes

import me.uport.sdk.AccountStorage
import me.uport.sdk.identity.HDAccount

/**
 * volatile account storage, usable in tests
 */
class InMemoryAccountStorage : AccountStorage {

    private val accounts = mapOf<String, HDAccount>().toMutableMap()

    override fun upsert(newAcc: HDAccount) {
        accounts[newAcc.handle] = newAcc
    }

    override fun upsertAll(list: Collection<HDAccount>) {
        list.forEach {
            accounts[it.handle] = it
        }
    }

    override fun get(handle: String): HDAccount? = accounts[handle]

    override fun delete(handle: String) {
        accounts.remove(handle)
    }

    override fun all(): List<HDAccount> = accounts.values.toList()
}