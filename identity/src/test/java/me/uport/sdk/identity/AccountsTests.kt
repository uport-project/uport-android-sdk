package me.uport.sdk.identity

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Test

class AccountsTests {

    @Test
    fun `can serialize and deserialize account`() {
        val refAccount = Account(
                "0xroot",
                "0xdevice",
                "0x1",
                "0xpublic",
                "",
                "",
                ""
        )

        val serialized = refAccount.toJson()

        val other = Account.fromJson(serialized)

        assert(other).isEqualTo(refAccount)
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

        assert(account).isNotNull()
        assert(account!!.isDefault!!).isFalse()
    }

    @Test
    fun `validates did from keypair account`() {

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
        assert(canResolve)
    }

    @Test
    fun `validates did from meta identity account`() {

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
        assert(canResolve)
    }

    @Test
    fun `throws error for invalid account type`() {

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
        assert {
            account!!.getDID()
        }.thrownError {
            isInstanceOf(IllegalStateException::class)
        }
    }


}
