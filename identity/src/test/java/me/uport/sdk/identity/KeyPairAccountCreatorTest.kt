package me.uport.sdk.identity

import assertk.all
import assertk.assert
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

            assert(account).all {
                isNotNull()
                isNotEqualTo(HDAccount.blank)
            }

            assert(account.address).isNotEmpty()
            assert(account.publicAddress).isNotEmpty()
            assert(account.deviceAddress).isNotEmpty()
        }
    }
}