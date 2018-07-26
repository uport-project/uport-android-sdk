package me.uport.sdk.identity

import android.content.Context
import android.support.test.InstrumentationRegistry
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.Networks
import org.junit.Assert.*
import org.junit.Test

import org.junit.Before

class KPAccountCreatorTest {

    lateinit var appContext: Context

    @Before
    fun run_before_every_test() {
        appContext = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun createAccount() {
        runBlocking {
            val account = KPAccountCreator(appContext).createAccount(Networks.rinkeby.network_id)
            assertNotNull(account)
            assertNotEquals(Account.blank, account)
            assertTrue(account.signerType == SignerType.KeyPair)
            assertTrue(account.address.isNotEmpty())
            assertTrue(account.publicAddress.isNotEmpty())
            assertTrue(account.deviceAddress.isNotEmpty())
        }
    }
}