package me.uport.sdk.fakes

import me.uport.sdk.AccountStorage
import me.uport.sdk.identity.Account

/**
 * volatile account storage, usable in tests
 */
class InMemoryAccountStorage : AccountStorage {

    override fun setAsDefault(accountHandle: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDefaultAccount(): Account? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val accounts = mapOf<String, Account>().toMutableMap()

    override fun upsert(newAcc: Account) {
        accounts[newAcc.handle] = newAcc
    }

    override fun upsertAll(list: Collection<Account>) {
        list.forEach {
            accounts[it.handle] = it
        }
    }

    override fun get(handle: String): Account? = accounts[handle]

    override fun delete(handle: String) {
        accounts.remove(handle)
    }

    override fun all(): List<Account> = accounts.values.toList()
}