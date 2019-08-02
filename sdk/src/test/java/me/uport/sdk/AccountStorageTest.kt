package me.uport.sdk

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import me.uport.sdk.fakes.InMemorySharedPrefs
import me.uport.sdk.identity.HDAccount
import org.junit.Test

class AccountStorageTest {

    @Test
    fun `can add and retrieve new account`() {
        val storage: AccountStorage = SharedPrefsAccountStorage(InMemorySharedPrefs())
        val newAcc = HDAccount("0xnewaccount", "", "", "")
        storage.upsert(newAcc)
        assertThat(storage.get("0xnewaccount")).isEqualTo(newAcc)
    }

    @Test
    fun `can show all accounts`() {
        val storage: AccountStorage = SharedPrefsAccountStorage(InMemorySharedPrefs())

        val accounts = (0..10).map {
            HDAccount("0x$it", "", "", "")
        }.map {
            storage.upsert(it)
            it
        }

        val allAccounts = storage.all()

        assertThat(allAccounts.containsAll(accounts))
    }

    @Test
    fun `can delete account`() {
        val storage: AccountStorage = SharedPrefsAccountStorage(InMemorySharedPrefs())

        val refAccount = HDAccount(
                "0xmyAccount",
                "device",
                "0x1",
                "0xpublic"
        )

        storage.upsert(refAccount)
        assertThat(storage.get(refAccount.handle)).isEqualTo(refAccount)

        storage.delete(refAccount.handle)

        assertThat(storage.get(refAccount.handle)).isNull()
        assertThat(storage.all()).doesNotContain(refAccount)
    }

    @Test
    fun `can overwrite account`() {
        val storage: AccountStorage = SharedPrefsAccountStorage(InMemorySharedPrefs())

        val refAccount = HDAccount(
                "0xmyAccount",
                "device",
                "0x1",
                "0xpublic"
        )

        storage.upsert(refAccount)

        val newAccount = refAccount.copy(network = "0x4")

        storage.upsert(newAccount)

        assertThat(storage.get(refAccount.handle)).all {
            isNotEqualTo(refAccount)
            isEqualTo(newAccount)
        }
        assertThat(storage.all()).all {
            doesNotContain(refAccount)
            contains(newAccount)
        }
    }

    @Test
    fun `can upsert all`() {
        val storage: AccountStorage = SharedPrefsAccountStorage(InMemorySharedPrefs())

        val accounts = (0..10).map {
            HDAccount("0x$it", "", "", "")
        }

        storage.upsertAll(accounts)

        val allAccounts = storage.all()

        assertThat(allAccounts.containsAll(accounts))
    }

    @Test
    fun `can set default account`() {
        val storage = SharedPrefsAccountStorage(InMemorySharedPrefs())

        val acc = HDAccount(
                "0xroot",
                "0xdevice",
                "0x1",
                "0xpublic"
        )

        storage.upsert(acc)

        storage.setAsDefault(acc.handle)

        assertThat(storage.getDefaultAccount()).isEqualTo(acc)
    }


    @Test
    fun `can save and fetch an account`() {
        val storage = SharedPrefsAccountStorage(InMemorySharedPrefs())

        val savedAcc = HDAccount(
                "0xroot",
                "0xdevice",
                "0x1",
                "0xpublic"
        )

        storage.upsert(savedAcc)

        val fetchedAcc = storage.get(savedAcc.handle)

        assertThat(savedAcc).isEqualTo(fetchedAcc)
    }

}