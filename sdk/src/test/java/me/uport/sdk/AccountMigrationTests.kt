package me.uport.sdk

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.uport.sdk.fakes.InMemorySharedPrefs
import me.uport.sdk.identity.HDAccount
import org.junit.Before
import org.junit.Test

class AccountMigrationTests {

    private val oldPrefs = InMemorySharedPrefs()

    private val KEY_ACCOUNTS = "accounts"

    private val KEY_DEFAULT_HANDLE = "default_account"

    @Before
    fun run_before_every_test() {

        val oldAccSet = setOf(
                """{
                    "uportRoot": "0x1f9339e3c08c120b4ee05d2f500d57000170664d",
                    "devKey": "0x95979bb3ee68420a0b105f6e3c0d5d0fc0466016",
                    "network": "0x04",
                    "proxy": "0x95979bb3ee68420a0b105f6e3c0d5d0fc0466016",
                    "manager": "",
                    "txRelay": "",
                    "fuelToken": "",
                    "signerType": "KeyPair",
                    "isDefault": false
                }""".trimIndent(),
                """{
                    "uportRoot": "0x64d13f1dba46a91c85509c60d8a7fc72ea7fcb74",
                    "devKey": "0xc94b48237f776360d06d4acef743798fb5792c4d",
                    "network": "0x4",
                    "proxy": "0xc94b48237f776360d06d4acef743798fb5792c4d",
                    "manager": "",
                    "txRelay": "",
                    "fuelToken": "",
                    "signerType": "KeyPair",
                    "isDefault": true
                }""".trimIndent(),
                """{
                    "uportRoot": "0xmyAccount",
                    "devKey": "device",
                    "network": "0x1",
                    "proxy": "0xpublic",
                    "manager": "",
                    "txRelay": "",
                    "fuelToken": "",
                    "signerType": "KeyPair",
                    "isDefault": false
                }""".trimIndent(),
                """{
                    "uportRoot": "0xa17d3cd4a72b563076c12027d827ad319800fe18",
                    "devKey": "0x85ffef1627c80df773081a4967f27bbbb732a8ad",
                    "network": "0x4",
                    "proxy": "0x85ffef1627c80df773081a4967f27bbbb732a8ad",
                    "manager": "",
                    "txRelay": "",
                    "fuelToken": "",
                    "signerType": "KeyPair",
                    "isDefault": false
                }""".trimIndent()
        )

        oldPrefs.edit()
                .putStringSet(KEY_ACCOUNTS, oldAccSet)
                .apply()

        oldPrefs.edit()
                .putString(KEY_DEFAULT_HANDLE, "0x64d13f1dba46a91c85509c60d8a7fc72ea7fcb74")
                .apply()

    }

    @Test
    fun old_storage_has_data_new_storage_is_empty() {

        val accountSet = oldPrefs.getStringSet(KEY_ACCOUNTS, null)
        assert(accountSet?.size).isEqualTo(4)

        val defaultAccountHandle = oldPrefs.getString(KEY_DEFAULT_HANDLE, "")
        assert(defaultAccountHandle).isEqualTo("0x64d13f1dba46a91c85509c60d8a7fc72ea7fcb74")

        val storage = SharedPrefsAccountStorage(InMemorySharedPrefs())
        assert(storage.all().size).isEqualTo(0)

        assert(storage.getDefaultAccount()).isNull()
    }

    @Test
    fun old_storage_is_empty_after_migration() {

        val storage = SharedPrefsAccountStorage(InMemorySharedPrefs())
        Uport.migrateAccounts(oldPrefs, storage)

        val accountSet = oldPrefs.getStringSet(KEY_ACCOUNTS, null)
        assert(accountSet?.size).isNull()

        val defaultAccountHandle = oldPrefs.getString(KEY_DEFAULT_HANDLE, "")
        assert(defaultAccountHandle).isEqualTo("")
    }

    @Test
    fun new_storage_has_data_after_migration() {

        val storage = SharedPrefsAccountStorage(InMemorySharedPrefs())
        Uport.migrateAccounts(oldPrefs, storage)

        assert(storage.all().size).isEqualTo(4)

        assert(storage.getDefaultAccount()?.handle).isEqualTo("0x64d13f1dba46a91c85509c60d8a7fc72ea7fcb74")
    }

    @Test
    fun new_storage_has_correct_data_after_migration() {

        val storage = SharedPrefsAccountStorage(InMemorySharedPrefs())
        Uport.migrateAccounts(oldPrefs, storage)

        val account1 = HDAccount(
                "0x1f9339e3c08c120b4ee05d2f500d57000170664d",
                "0x95979bb3ee68420a0b105f6e3c0d5d0fc0466016",
                "0x04",
                "0x95979bb3ee68420a0b105f6e3c0d5d0fc0466016"
        )

        val account2 = HDAccount(
                "0x64d13f1dba46a91c85509c60d8a7fc72ea7fcb74",
                "0xc94b48237f776360d06d4acef743798fb5792c4d",
                "0x4",
                "0xc94b48237f776360d06d4acef743798fb5792c4d"
        )

        val account3 = HDAccount(
                "0xmyAccount",
                "device",
                "0x1",
                "0xpublic"
        )

        val account4 = HDAccount(
                "0xa17d3cd4a72b563076c12027d827ad319800fe18",
                "0x85ffef1627c80df773081a4967f27bbbb732a8ad",
                "0x4",
                "0x85ffef1627c80df773081a4967f27bbbb732a8ad"
        )

        assert(storage.all().contains(account1))

        assert(storage.all().contains(account2))

        assert(storage.all().contains(account3))

        assert(storage.all().contains(account4))
    }
}