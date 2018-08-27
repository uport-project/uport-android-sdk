package me.uport.sdk

import me.uport.sdk.identity.Account

interface AccountStorage {
    fun upsert(newAcc: Account)

    fun get(handle: String): Account?

    fun delete(handle: String)
}

class InMemoryAccountStorage : AccountStorage {

    override fun upsert(newAcc: Account) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(handle: String): Account? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(handle: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}