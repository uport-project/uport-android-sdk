package me.uport.sdk

import me.uport.sdk.fakes.InMemorySharedPrefs
import me.uport.sdk.identity.Account
import org.junit.Assert.*
import org.junit.Test

class AccountStorageTest {

    @Test
    fun `can add and retrieve new account`() {
        val storage: AccountStorage = SharedPrefsAcountStorage(InMemorySharedPrefs())
        val newAcc = Account("0xnewaccount", "", "", "", "", "", "")
        storage.upsert(newAcc)
        assertEquals(newAcc, storage.get("0xnewaccount"))
    }

    @Test
    fun `can show all accounts`() {
        val storage: AccountStorage = SharedPrefsAcountStorage(InMemorySharedPrefs())

        val accounts = (0..10).map {
            Account("0x$it", "", "", "", "", "", "")
        }.map {
            storage.upsert(it)
            it
        }

        val allAccounts = storage.all()

        assertTrue(allAccounts.containsAll(accounts))
    }

    @Test
    fun `can delete account`() {
        val storage: AccountStorage = SharedPrefsAcountStorage(InMemorySharedPrefs())

        val account = Account(
                "0xmyAccount",
                "device",
                "0x1",
                "0xpublic",
                "",
                "",
                ""
        )

        storage.upsert(account)

        storage.upsert(account)
        assertEquals(account, storage.get(account.handle))

        storage.delete(account.handle)

        assertNull(storage.get(account.handle))
        assertFalse(storage.all().contains(account))
    }

    @Test
    fun `can overwrite account`() {
        val storage: AccountStorage = SharedPrefsAcountStorage(InMemorySharedPrefs())

        val account = Account(
                "0xmyAccount",
                "device",
                "0x1",
                "0xpublic",
                "",
                "",
                ""
        )

        storage.upsert(account)

        val newAccount = account.copy(isDefault = true)

        storage.upsert(newAccount)

        assertNotEquals(account, storage.get(account.handle))
        assertEquals(newAccount, storage.get(account.handle))

        assertFalse(storage.all().contains(account))
        assertTrue(storage.all().contains(newAccount))
    }

    @Test
    fun `can upsert all`() {
        val storage: AccountStorage = SharedPrefsAcountStorage(InMemorySharedPrefs())

        val accounts = (0..10).map {
            Account("0x$it", "", "", "", "", "", "")
        }

        storage.upsertAll(accounts)

        val allAccounts = storage.all()

        assertTrue(allAccounts.containsAll(accounts))
    }

}