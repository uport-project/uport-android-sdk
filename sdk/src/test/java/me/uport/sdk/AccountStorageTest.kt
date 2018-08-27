package me.uport.sdk

import me.uport.sdk.identity.Account
import org.junit.Assert
import org.junit.Test

class AccountStorageTest {

    @Test
    fun `can add new account`() {
        val storage: AccountStorage = InMemoryAccountStorage()
        val newAcc = Account("0xnewaccount", "", "", "", "", "", "")
        storage.upsert(newAcc)
        Assert.assertEquals(newAcc, storage.get("0xnewaccount"))
    }

}