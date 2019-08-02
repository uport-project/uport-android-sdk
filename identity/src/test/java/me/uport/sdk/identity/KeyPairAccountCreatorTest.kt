package me.uport.sdk.identity

import assertk.all
import assertk.assertThat
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.Networks
import org.junit.Test

class KeyPairAccountCreatorTest {

    @Test
    fun createAccount() {
        runBlocking {
            val account = KeyPairAccountCreator("0x1234").createAccount(Networks.rinkeby.networkId)

            assertThat(account).all {
                isNotNull()
                isNotEqualTo(HDAccount.blank)
            }

            assertThat(account.address).isNotEmpty()
            assertThat(account.publicAddress).isNotEmpty()
            assertThat(account.deviceAddress).isNotEmpty()
        }
    }
}