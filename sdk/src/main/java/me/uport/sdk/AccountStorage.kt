package me.uport.sdk

import me.uport.sdk.identity.Account

interface AccountStorage {
    fun upsert(newAcc: Account)

    fun get(handle: String): Account?

    fun delete(handle: String)
}

class InMemoryAccountStorage : AccountStorage {

    private val accounts = mapOf<String, Account>().toMutableMap()

    override fun upsert(newAcc: Account) {
        accounts[newAcc.handle] = newAcc
    }

    override fun get(handle: String): Account? {
        return accounts[handle]
    }

    override fun delete(handle: String) {
        accounts.remove(handle)
    }
}