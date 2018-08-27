package me.uport.sdk.identity

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class AccountSerializationTests {

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
        assertFalse(account.isDefault)
    }

}