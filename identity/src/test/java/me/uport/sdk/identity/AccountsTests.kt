package me.uport.sdk.identity

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
              "type":"KeyPair"
            }""".trimIndent()

        val account = Account.fromJson(serializedAccount)

        assertNotNull(account)

        account!!

        assertNotNull(account.isDefault)

        assertFalse(account.isDefault!!)
    }

}