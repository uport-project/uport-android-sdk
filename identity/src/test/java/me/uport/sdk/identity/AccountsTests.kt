package me.uport.sdk.identity

import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Assert.*
import org.junit.Test

class AccountsTests {

    @Test
    fun `can serialize and deserialize account`() {
        val acc = Account(
                "0xroot",
                "0xdevice",
                "0x1",
                "0xpublic",
                "",
                "",
                ""
        )

        val serialized = acc.toJson()

        println(serialized)

        val other = Account.fromJson(serialized)

        assertEquals(acc, other)
    }

    @Test
    fun `can deserialize account with optional field`() {

        //language=JSON
        val serializedAccount = """
            {
              "uportRoot":"0xrootAddress",
              "devKey":"0xaddress",
              "network":"0x4",
              "proxy":"0xpublicaddress",
              "manager":"0xidentityManagerAddress",
              "txRelay":"0xtxRelayAddress",
              "fuelToken":"base64FuelToken",
              "signerType":"KeyPair"
            }""".trimIndent()

        val account = Account.fromJson(serializedAccount)

        assertNotNull(account)

        account!!

        assertNotNull(account.isDefault)

        assertFalse(account.isDefault!!)
    }

    @Test
    fun validate_did_from_keypair_account() {

        val serializedAccount = """
            {
                "uportRoot": "0x1f9339e3c08c120b4ee05d2f500d57000170664d",
                "devKey": "0x95979bb3ee68420a0b105f6e3c0d5d0fc0466016",
                "network": "0x04",
                "proxy": "0x95979bb3ee68420a0b105f6e3c0d5d0fc0466016",
                "manager": "",
                "txRelay": "",
                "fuelToken": "",
                "signerType": "KeyPair",
                "isDefault": true
            }""".trimIndent()

        val account = Account.fromJson(serializedAccount)
        val defaultRPC = JsonRPC(Networks.mainnet.rpcUrl)
        val canResolve = EthrDIDResolver(defaultRPC).canResolve(account!!.getDID())
        assertTrue(canResolve)
    }

    @Test
    fun validate_did_from_meta_identity_account() {

        val serializedAccount = """
            {
              "uportRoot":"0xrootAddress",
              "devKey":"0xaddress",
              "network":"0x4",
              "proxy":"0xpublicaddress",
              "manager":"0xidentityManagerAddress",
              "txRelay":"0xtxRelayAddress",
              "fuelToken":"base64FuelToken",
              "signerType":"MetaIdentityManager"
            }""".trimIndent()

        val account = Account.fromJson(serializedAccount)
        val canResolve = UportDIDResolver().canResolve(account!!.getDID())
        assertTrue(canResolve)
    }

    @Test(expected = IllegalStateException::class)
    fun throws_error_for_invalid_account_type() {

        val serializedAccount = """
            {
              "uportRoot":"0xrootAddress",
              "devKey":"0xaddress",
              "network":"0x4",
              "proxy":"0xpublicaddress",
              "manager":"0xidentityManagerAddress",
              "txRelay":"0xtxRelayAddress",
              "fuelToken":"base64FuelToken",
              "signerType":"Proxy"
            }""".trimIndent()

        val account = Account.fromJson(serializedAccount)
        account!!.getDID()
    }


}
